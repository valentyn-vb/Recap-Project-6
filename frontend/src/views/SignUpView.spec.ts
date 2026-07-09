import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory, type Router } from 'vue-router'
import { beforeEach, describe, expect, it, vi, type MockInstance } from 'vitest'
import { createAppRouter } from '../router'
import { ProfileSaveError, useAuthStore } from '../stores/auth'
import SignUpView from './SignUpView.vue'

function axios409(): AxiosError {
  return new AxiosError('conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
    status: 409,
    statusText: 'Conflict',
    data: { message: 'Email already in use' },
    headers: {},
    config: {} as never,
  })
}

async function mountSignUp(): Promise<{
  wrapper: ReturnType<typeof mount>
  router: Router
  registerSpy: MockInstance
}> {
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useAuthStore()
  const registerSpy = vi.spyOn(store, 'register')
  const router = createAppRouter(createMemoryHistory())
  await router.push('/signup')
  await router.isReady()
  const wrapper = mount(SignUpView, {
    global: { plugins: [pinia, router, PrimeVue] },
  })
  return { wrapper, router, registerSpy }
}

async function fillForm(wrapper: ReturnType<typeof mount>): Promise<void> {
  await wrapper.get('[data-testid="email"] input, input[data-testid="email"]').setValue('alice@example.com')
  await wrapper.get('[data-testid="password"] input').setValue('s3cret-password')
  await wrapper.get('[data-testid="name"] input, input[data-testid="name"]').setValue('Alice')
  await wrapper.get('[data-testid="cohort"] input, input[data-testid="cohort"]').setValue('NF-2026')
  await wrapper.get('[data-testid="focus-areas"] input, input[data-testid="focus-areas"]').setValue('vue, spring')
}

describe('SignUpView', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('renders account and profile fields', async () => {
    const { wrapper } = await mountSignUp()

    for (const field of ['email', 'password', 'name', 'cohort', 'focus-areas']) {
      expect(wrapper.find(`[data-testid="${field}"]`).exists()).toBe(true)
    }
  })

  it('submits credentials and profile data and navigates home on success', async () => {
    const { wrapper, router, registerSpy } = await mountSignUp()
    registerSpy.mockResolvedValue(undefined)

    await fillForm(wrapper)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(registerSpy).toHaveBeenCalledWith('alice@example.com', 's3cret-password', {
      name: 'Alice',
      cohort: 'NF-2026',
      focusAreas: ['vue', 'spring'],
    })
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('shows a duplicate-email error on 409 and stays on the page', async () => {
    const { wrapper, router, registerSpy } = await mountSignUp()
    registerSpy.mockRejectedValue(axios409())

    await fillForm(wrapper)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('already in use')
    expect(router.currentRoute.value.path).toBe('/signup')
  })

  it('does not submit when email or password is empty', async () => {
    const { wrapper, registerSpy } = await mountSignUp()

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(registerSpy).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('required')
  })

  it('still navigates home when only the profile save failed', async () => {
    const { wrapper, router, registerSpy } = await mountSignUp()
    registerSpy.mockRejectedValue(new ProfileSaveError(new Error('500')))

    await fillForm(wrapper)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/')
  })
})

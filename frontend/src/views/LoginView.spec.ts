import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError } from 'axios'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory, type Router } from 'vue-router'
import { beforeEach, describe, expect, it, vi, type MockInstance } from 'vitest'
import { createAppRouter } from '../router'
import { useAuthStore } from '../stores/auth'
import LoginView from './LoginView.vue'

function axios401(): AxiosError {
  return new AxiosError('unauthorized', 'ERR_BAD_REQUEST', undefined, undefined, {
    status: 401,
    statusText: 'Unauthorized',
    data: { message: 'Invalid email or password' },
    headers: {},
    config: {} as never,
  })
}

async function mountLogin(initialRoute = '/login'): Promise<{
  wrapper: ReturnType<typeof mount>
  router: Router
  loginSpy: MockInstance
}> {
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useAuthStore()
  const loginSpy = vi.spyOn(store, 'login')
  const router = createAppRouter(createMemoryHistory())
  await router.push(initialRoute)
  await router.isReady()
  const wrapper = mount(LoginView, {
    global: { plugins: [pinia, router, PrimeVue] },
  })
  return { wrapper, router, loginSpy }
}

async function fillAndSubmit(wrapper: ReturnType<typeof mount>): Promise<void> {
  await wrapper.get('[data-testid="email"]').setValue('alice@example.com')
  await wrapper.get('[data-testid="password"] input').setValue('s3cret-password')
  await wrapper.get('form').trigger('submit')
  await flushPromises()
}

describe('LoginView', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('renders email and password fields and a signup link', async () => {
    const { wrapper } = await mountLogin()

    expect(wrapper.find('[data-testid="email"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="password"]').exists()).toBe(true)
    expect(wrapper.find('a[href="/signup"]').exists()).toBe(true)
  })

  it('logs in and navigates home by default', async () => {
    const { wrapper, router, loginSpy } = await mountLogin()
    loginSpy.mockImplementation(async () => {
      useAuthStore().setSession('jwt', 'alice@example.com')
    })

    await fillAndSubmit(wrapper)

    expect(loginSpy).toHaveBeenCalledWith('alice@example.com', 's3cret-password')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('honors the redirect query after login', async () => {
    const { wrapper, router, loginSpy } = await mountLogin('/login?redirect=/profile')
    loginSpy.mockImplementation(async () => {
      useAuthStore().setSession('jwt', 'alice@example.com')
    })

    await fillAndSubmit(wrapper)

    expect(router.currentRoute.value.path).toBe('/profile')
  })

  it('shows a generic error on 401 and stays on the page', async () => {
    const { wrapper, router, loginSpy } = await mountLogin()
    loginSpy.mockRejectedValue(axios401())

    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Invalid email or password')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(useAuthStore().isAuthenticated).toBe(false)
  })
})

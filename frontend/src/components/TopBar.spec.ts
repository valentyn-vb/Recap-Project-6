import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory, type Router } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { saveAuth } from '../lib/tokenStorage'
import { createAppRouter } from '../router'
import { useAuthStore } from '../stores/auth'
import TopBar from './TopBar.vue'

async function mountTopBar(): Promise<{ wrapper: ReturnType<typeof mount>; router: Router }> {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createAppRouter(createMemoryHistory())
  const wrapper = mount(TopBar, {
    global: {
      plugins: [pinia, router, PrimeVue],
    },
  })
  await router.isReady()
  return { wrapper, router }
}

describe('TopBar', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('shows a sign-in button and no user info when logged out', async () => {
    const { wrapper } = await mountTopBar()

    expect(wrapper.text()).toContain('Sign in')
    expect(wrapper.text()).not.toContain('Log out')
    expect(wrapper.text()).not.toContain('@')
  })

  it('navigates to /login when sign in is clicked', async () => {
    const { wrapper, router } = await mountTopBar()

    await wrapper.get('[data-testid="sign-in"]').trigger('click')
    await flushPromises()

    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('shows the user email, a profile link and a log-out button when logged in', async () => {
    saveAuth({ token: 'jwt', email: 'alice@example.com' })
    const { wrapper } = await mountTopBar()

    expect(wrapper.text()).toContain('alice@example.com')
    expect(wrapper.text()).toContain('Log out')
    expect(wrapper.find('[data-testid="sign-in"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="profile-link"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="goals-link"]').exists()).toBe(true)
  })

  it('does not show the goals link when logged out', async () => {
    const { wrapper } = await mountTopBar()

    expect(wrapper.find('[data-testid="goals-link"]').exists()).toBe(false)
  })

  it('logs out and navigates home when log out is clicked', async () => {
    saveAuth({ token: 'jwt', email: 'alice@example.com' })
    const { wrapper, router } = await mountTopBar()
    await router.push('/profile')

    await wrapper.get('[data-testid="log-out"]').trigger('click')
    await flushPromises()

    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(router.currentRoute.value.path).toBe('/')
  })
})

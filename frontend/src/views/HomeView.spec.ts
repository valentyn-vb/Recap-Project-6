import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { saveAuth } from '../lib/tokenStorage'
import { createAppRouter } from '../router'
import HomeView from './HomeView.vue'

async function mountHome() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createAppRouter(createMemoryHistory())
  await router.push('/')
  return mount(HomeView, { global: { plugins: [pinia, router, PrimeVue] } })
}

describe('HomeView', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('shows the intro with sign-up and login links when logged out', async () => {
    const wrapper = await mountHome()

    expect(wrapper.text()).toContain('Learning Companion')
    expect(wrapper.find('a[href="/signup"]').exists()).toBe(true)
    expect(wrapper.find('a[href="/login"]').exists()).toBe(true)
  })

  it('greets the logged-in user with a profile link and no signup CTA', async () => {
    saveAuth({ token: 'jwt', email: 'alice@example.com' })
    const wrapper = await mountHome()

    expect(wrapper.text()).toContain('alice@example.com')
    expect(wrapper.find('a[href="/profile"]').exists()).toBe(true)
    expect(wrapper.find('a[href="/signup"]').exists()).toBe(false)
  })
})

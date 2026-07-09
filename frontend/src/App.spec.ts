import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'
import App from './App.vue'
import { createAppRouter } from './router'

async function mountApp() {
  const router = createAppRouter(createMemoryHistory())
  const wrapper = mount(App, {
    global: {
      plugins: [createPinia(), router, PrimeVue],
    },
  })
  await router.isReady()
  return { wrapper, router }
}

describe('App', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('renders the home route inside the router view', async () => {
    const { wrapper } = await mountApp()

    expect(wrapper.text()).toContain('Learning Companion')
  })

  it('swaps the rendered view when navigating', async () => {
    const { wrapper, router } = await mountApp()

    await router.push('/login')
    await router.isReady()

    expect(wrapper.text()).toContain('Log in')
  })
})

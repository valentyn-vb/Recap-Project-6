import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'
import { saveAuth } from '../lib/tokenStorage'
import { useAuthStore } from '../stores/auth'
import { createAppRouter } from './index'

describe('router auth guard', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('redirects a logged-out visitor from /profile to /login with a redirect query', async () => {
    const router = createAppRouter(createMemoryHistory())

    await router.push('/profile')

    expect(router.currentRoute.value.path).toBe('/login')
    expect(router.currentRoute.value.query.redirect).toBe('/profile')
  })

  it('lets an authenticated user open /profile', async () => {
    saveAuth({ token: 'jwt', email: 'alice@example.com' })
    useAuthStore()
    const router = createAppRouter(createMemoryHistory())

    await router.push('/profile')

    expect(router.currentRoute.value.path).toBe('/profile')
  })

  it('redirects an authenticated user away from /login and /signup', async () => {
    saveAuth({ token: 'jwt', email: 'alice@example.com' })
    useAuthStore()
    const router = createAppRouter(createMemoryHistory())

    await router.push('/login')
    expect(router.currentRoute.value.path).toBe('/')

    await router.push('/signup')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('leaves home, login and signup open for logged-out visitors', async () => {
    const router = createAppRouter(createMemoryHistory())

    await router.push('/')
    expect(router.currentRoute.value.path).toBe('/')

    await router.push('/login')
    expect(router.currentRoute.value.path).toBe('/login')

    await router.push('/signup')
    expect(router.currentRoute.value.path).toBe('/signup')
  })
})

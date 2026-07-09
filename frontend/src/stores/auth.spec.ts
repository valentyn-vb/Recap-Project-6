import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as authApi from '../api/authApi'
import { loadAuth, saveAuth } from '../lib/tokenStorage'
import { ProfileSaveError, useAuthStore } from './auth'

vi.mock('../api/authApi', () => ({
  register: vi.fn(),
  login: vi.fn(),
  getProfile: vi.fn(),
  saveProfile: vi.fn(),
}))

const mockedApi = vi.mocked(authApi)

const profilePayload = { name: 'Alice', cohort: 'NF-2026', focusAreas: ['vue'] }

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('starts logged out with empty storage', () => {
    const store = useAuthStore()

    expect(store.token).toBeNull()
    expect(store.email).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('hydrates from a persisted snapshot so a refresh keeps you logged in', () => {
    saveAuth({ token: 'persisted-jwt', email: 'alice@example.com' })

    const store = useAuthStore()

    expect(store.token).toBe('persisted-jwt')
    expect(store.email).toBe('alice@example.com')
    expect(store.isAuthenticated).toBe(true)
  })

  it('login stores token and email and persists them', async () => {
    mockedApi.login.mockResolvedValue({ token: 'fresh-jwt' })
    const store = useAuthStore()

    await store.login('alice@example.com', 'pw')

    expect(mockedApi.login).toHaveBeenCalledWith({ email: 'alice@example.com', password: 'pw' })
    expect(store.token).toBe('fresh-jwt')
    expect(store.email).toBe('alice@example.com')
    expect(store.isAuthenticated).toBe(true)
    expect(loadAuth()).toEqual({ token: 'fresh-jwt', email: 'alice@example.com' })
  })

  it('login failure leaves the store logged out and propagates', async () => {
    const failure = new Error('401')
    mockedApi.login.mockRejectedValue(failure)
    const store = useAuthStore()

    await expect(store.login('alice@example.com', 'bad')).rejects.toBe(failure)
    expect(store.isAuthenticated).toBe(false)
    expect(loadAuth()).toBeNull()
  })

  it('register stores the token before saving the profile', async () => {
    const order: string[] = []
    mockedApi.register.mockImplementation(async () => {
      order.push('register')
      return { id: 1, email: 'alice@example.com', token: 'new-jwt' }
    })
    mockedApi.saveProfile.mockImplementation(async () => {
      order.push('saveProfile')
      // the PUT needs the bearer token, so it must already be persisted
      expect(loadAuth()?.token).toBe('new-jwt')
      return { id: 1, ...profilePayload }
    })
    const store = useAuthStore()

    await store.register('alice@example.com', 'pw', profilePayload)

    expect(order).toEqual(['register', 'saveProfile'])
    expect(mockedApi.saveProfile).toHaveBeenCalledWith(profilePayload)
    expect(store.isAuthenticated).toBe(true)
  })

  it('register failure leaves the store logged out and propagates', async () => {
    const failure = new Error('409')
    mockedApi.register.mockRejectedValue(failure)
    const store = useAuthStore()

    await expect(store.register('taken@example.com', 'pw', profilePayload)).rejects.toBe(failure)
    expect(store.isAuthenticated).toBe(false)
    expect(loadAuth()).toBeNull()
    expect(mockedApi.saveProfile).not.toHaveBeenCalled()
  })

  it('profile-save failure after successful register keeps you logged in but signals it', async () => {
    mockedApi.register.mockResolvedValue({ id: 1, email: 'alice@example.com', token: 'new-jwt' })
    mockedApi.saveProfile.mockRejectedValue(new Error('500'))
    const store = useAuthStore()

    await expect(store.register('alice@example.com', 'pw', profilePayload))
      .rejects.toBeInstanceOf(ProfileSaveError)
    expect(store.isAuthenticated).toBe(true)
    expect(loadAuth()?.token).toBe('new-jwt')
  })

  it('logout clears state and storage', async () => {
    mockedApi.login.mockResolvedValue({ token: 'jwt' })
    const store = useAuthStore()
    await store.login('alice@example.com', 'pw')

    store.logout()

    expect(store.token).toBeNull()
    expect(store.email).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(loadAuth()).toBeNull()
  })
})

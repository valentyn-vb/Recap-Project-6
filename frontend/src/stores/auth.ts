import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister, saveProfile } from '../api/authApi'
import type { ProfilePayload } from '../api/types'
import { clearAuth, loadAuth, saveAuth } from '../lib/tokenStorage'

/** Register succeeded but the follow-up profile save failed — the user IS logged in. */
export class ProfileSaveError extends Error {
  constructor(cause: unknown) {
    super('Account was created, but saving the profile failed')
    this.cause = cause
  }
}

interface AuthState {
  token: string | null
  email: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => {
    const persisted = loadAuth()
    return {
      token: persisted?.token ?? null,
      email: persisted?.email ?? null,
    }
  },

  getters: {
    isAuthenticated: (state): boolean => state.token !== null,
  },

  actions: {
    async login(email: string, password: string): Promise<void> {
      const { token } = await apiLogin({ email, password })
      this.setSession(token, email)
    },

    async register(email: string, password: string, profile: ProfilePayload): Promise<void> {
      const { token } = await apiRegister({ email, password })
      // persist first: the profile PUT needs the bearer token
      this.setSession(token, email)
      try {
        await saveProfile(profile)
      } catch (error: unknown) {
        throw new ProfileSaveError(error)
      }
    },

    logout(): void {
      this.token = null
      this.email = null
      clearAuth()
    },

    setSession(token: string, email: string): void {
      this.token = token
      this.email = email
      saveAuth({ token, email })
    },
  },
})

import axios, { type AxiosInstance } from 'axios'
import { loadAuth } from '../lib/tokenStorage'

export interface HttpOptions {
  getToken: () => string | null
  onUnauthorized: () => void
}

export function createHttp({ getToken, onUnauthorized }: HttpOptions): AxiosInstance {
  const instance = axios.create({ baseURL: '/api' })

  instance.interceptors.request.use((config) => {
    const token = getToken()
    if (token !== null) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  instance.interceptors.response.use(
    (response) => response,
    (error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        onUnauthorized()
      }
      return Promise.reject(error)
    },
  )

  return instance
}

let unauthorizedHandler: (() => void) | null = null

/** Wired at bootstrap (main.ts) to log out and redirect to the login page. */
export function setUnauthorizedHandler(handler: () => void): void {
  unauthorizedHandler = handler
}

export const http = createHttp({
  getToken: () => loadAuth()?.token ?? null,
  onUnauthorized: () => unauthorizedHandler?.(),
})

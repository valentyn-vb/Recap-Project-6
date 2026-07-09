import { AxiosError, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { describe, expect, it, vi } from 'vitest'
import { createHttp } from './http'

function adapterReturning(status: number): { adapter: AxiosAdapter; seen: InternalAxiosRequestConfig[] } {
  const seen: InternalAxiosRequestConfig[] = []
  const adapter: AxiosAdapter = (config) => {
    seen.push(config)
    const response = { data: {}, status, statusText: String(status), headers: {}, config }
    if (status >= 200 && status < 300) {
      return Promise.resolve(response)
    }
    // real axios adapters reject non-2xx with an AxiosError carrying the response
    return Promise.reject(new AxiosError(`Request failed with status code ${status}`,
      'ERR_BAD_REQUEST', config, null, response))
  }
  return { adapter, seen }
}

describe('createHttp', () => {
  it('uses /api as base url', () => {
    const http = createHttp({ getToken: () => null, onUnauthorized: vi.fn() })

    expect(http.defaults.baseURL).toBe('/api')
  })

  it('adds a bearer authorization header when a token is present', async () => {
    const { adapter, seen } = adapterReturning(200)
    const http = createHttp({ getToken: () => 'my-token', onUnauthorized: vi.fn() })

    await http.get('/profile', { adapter })

    expect(seen[0]?.headers.Authorization).toBe('Bearer my-token')
  })

  it('sends no authorization header when no token is present', async () => {
    const { adapter, seen } = adapterReturning(200)
    const http = createHttp({ getToken: () => null, onUnauthorized: vi.fn() })

    await http.get('/profile', { adapter })

    expect(seen[0]?.headers.Authorization).toBeUndefined()
  })

  it('invokes onUnauthorized exactly once on a 401 and still rejects', async () => {
    const { adapter } = adapterReturning(401)
    const onUnauthorized = vi.fn()
    const http = createHttp({ getToken: () => 'stale-token', onUnauthorized })

    await expect(http.get('/profile', { adapter })).rejects.toThrow()
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('does not invoke onUnauthorized on other errors', async () => {
    const { adapter } = adapterReturning(500)
    const onUnauthorized = vi.fn()
    const http = createHttp({ getToken: () => 'token', onUnauthorized })

    await expect(http.get('/profile', { adapter })).rejects.toThrow()
    expect(onUnauthorized).not.toHaveBeenCalled()
  })
})

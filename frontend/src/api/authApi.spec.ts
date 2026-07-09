import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getProfile, login, register, saveProfile } from './authApi'
import { http } from './http'

vi.mock('./http', () => ({
  http: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
  },
}))

const mockedHttp = vi.mocked(http)

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('register posts credentials and returns the created identity with token', async () => {
    mockedHttp.post.mockResolvedValue({
      data: { id: 1, email: 'alice@example.com', token: 'jwt' },
    })

    const result = await register({ email: 'alice@example.com', password: 'pw' })

    expect(mockedHttp.post).toHaveBeenCalledWith('/auth/register', {
      email: 'alice@example.com',
      password: 'pw',
    })
    expect(result).toEqual({ id: 1, email: 'alice@example.com', token: 'jwt' })
  })

  it('login posts credentials and returns the token', async () => {
    mockedHttp.post.mockResolvedValue({ data: { token: 'jwt' } })

    const result = await login({ email: 'alice@example.com', password: 'pw' })

    expect(mockedHttp.post).toHaveBeenCalledWith('/auth/login', {
      email: 'alice@example.com',
      password: 'pw',
    })
    expect(result).toEqual({ token: 'jwt' })
  })

  it('getProfile fetches the own profile', async () => {
    mockedHttp.get.mockResolvedValue({
      data: { id: 5, name: 'Alice', cohort: 'NF-2026', focusAreas: ['vue'] },
    })

    const result = await getProfile()

    expect(mockedHttp.get).toHaveBeenCalledWith('/profile')
    expect(result).toEqual({ id: 5, name: 'Alice', cohort: 'NF-2026', focusAreas: ['vue'] })
  })

  it('saveProfile puts the profile payload', async () => {
    mockedHttp.put.mockResolvedValue({
      data: { id: 5, name: 'Alice', cohort: 'NF-2026', focusAreas: ['vue', 'spring'] },
    })

    const result = await saveProfile({ name: 'Alice', cohort: 'NF-2026', focusAreas: ['vue', 'spring'] })

    expect(mockedHttp.put).toHaveBeenCalledWith('/profile', {
      name: 'Alice',
      cohort: 'NF-2026',
      focusAreas: ['vue', 'spring'],
    })
    expect(result.focusAreas).toEqual(['vue', 'spring'])
  })

  it('propagates errors from the http layer unchanged', async () => {
    const failure = new Error('network down')
    mockedHttp.post.mockRejectedValue(failure)

    await expect(login({ email: 'a@b.c', password: 'x' })).rejects.toBe(failure)
  })
})

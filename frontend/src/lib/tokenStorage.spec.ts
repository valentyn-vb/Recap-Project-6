import { beforeEach, describe, expect, it } from 'vitest'
import { clearAuth, loadAuth, saveAuth, STORAGE_KEY } from './tokenStorage'

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('saves and loads an auth snapshot', () => {
    saveAuth({ token: 'jwt-token', email: 'alice@example.com' })

    expect(loadAuth()).toEqual({ token: 'jwt-token', email: 'alice@example.com' })
  })

  it('returns null when nothing is stored', () => {
    expect(loadAuth()).toBeNull()
  })

  it('returns null instead of throwing on corrupt JSON', () => {
    localStorage.setItem(STORAGE_KEY, '{not json')

    expect(loadAuth()).toBeNull()
  })

  it('returns null when stored value has the wrong shape', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ foo: 'bar' }))

    expect(loadAuth()).toBeNull()
  })

  it('clears the stored snapshot', () => {
    saveAuth({ token: 'jwt-token', email: 'alice@example.com' })
    clearAuth()

    expect(loadAuth()).toBeNull()
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull()
  })
})

export const STORAGE_KEY = 'learning-companion.auth'

export interface AuthSnapshot {
  token: string
  email: string
}

function isAuthSnapshot(value: unknown): value is AuthSnapshot {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as Record<string, unknown>).token === 'string' &&
    typeof (value as Record<string, unknown>).email === 'string'
  )
}

export function saveAuth(snapshot: AuthSnapshot): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot))
}

export function loadAuth(): AuthSnapshot | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw === null) {
    return null
  }
  try {
    const parsed: unknown = JSON.parse(raw)
    return isAuthSnapshot(parsed) ? parsed : null
  } catch {
    return null
  }
}

export function clearAuth(): void {
  localStorage.removeItem(STORAGE_KEY)
}

export type StoredAuth = {
  token: string
  expiresAt: string
  username: string
  roles: string[]
  tenant?: string
}

const STORAGE_KEY = 'eip.auth'

export const getStoredAuth = () => {
  if (typeof window === 'undefined') {
    return null
  }
  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as StoredAuth
  } catch {
    return null
  }
}

export const setStoredAuth = (auth: StoredAuth) => {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(auth))
}

export const clearStoredAuth = () => {
  window.localStorage.removeItem(STORAGE_KEY)
}

export const isAuthExpired = (auth: StoredAuth) => {
  if (!auth.expiresAt) {
    return false
  }
  const expiresAtMs = new Date(auth.expiresAt).getTime()
  if (!Number.isFinite(expiresAtMs)) {
    return true
  }
  return Date.now() > expiresAtMs
}

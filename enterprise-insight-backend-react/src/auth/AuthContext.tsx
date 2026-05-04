import { type ReactNode, useEffect, useMemo, useState } from 'react'
import { loginUser } from '../api/auth'
import { AuthContext } from './authContextValue'
import {
  clearStoredAuth,
  getStoredAuth,
  isAuthExpired,
  setStoredAuth,
  type StoredAuth,
} from './authStorage'

const resolveInitialAuth = () => {
  const stored = getStoredAuth()
  if (!stored) {
    return null
  }
  if (isAuthExpired(stored)) {
    clearStoredAuth()
    return null
  }
  return stored
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<StoredAuth | null>(() => resolveInitialAuth())

  useEffect(() => {
    if (!auth?.expiresAt) {
      return
    }
    const msUntilExpiry = new Date(auth.expiresAt).getTime() - Date.now()
    const timeoutId = window.setTimeout(() => {
      clearStoredAuth()
      setAuth(null)
    }, Number.isFinite(msUntilExpiry) ? Math.max(msUntilExpiry, 0) : 0)

    return () => window.clearTimeout(timeoutId)
  }, [auth])

  const login = async (username: string, password: string) => {
    const data = await loginUser({ username, password })

    const nextAuth: StoredAuth = {
      token: data.token,
      expiresAt: data.expiresAt,
      roles: data.roles,
      username,
      tenant: 'demo-tenant',
    }
    setAuth(nextAuth)
    setStoredAuth(nextAuth)
  }

  const logout = () => {
    clearStoredAuth()
    setAuth(null)
  }

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: Boolean(auth),
      login,
      logout,
    }),
    [auth],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

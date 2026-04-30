import {
  createContext,
  type ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { apiRequest } from '../api/client'
import {
  clearStoredAuth,
  getStoredAuth,
  isAuthExpired,
  setStoredAuth,
  type StoredAuth,
} from './authStorage'

type LoginResponse = {
  token: string
  expiresAt: string
  roles: string[]
}

type AuthContextValue = {
  auth: StoredAuth | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

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
    if (!Number.isFinite(msUntilExpiry) || msUntilExpiry <= 0) {
      clearStoredAuth()
      setAuth(null)
      return
    }
    const timeoutId = window.setTimeout(() => {
      clearStoredAuth()
      setAuth(null)
    }, msUntilExpiry)

    return () => window.clearTimeout(timeoutId)
  }, [auth])

  const login = async (username: string, password: string) => {
    const data = await apiRequest<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: { username, password },
    })

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

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}

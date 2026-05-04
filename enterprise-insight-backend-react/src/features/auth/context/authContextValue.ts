import { createContext } from 'react'
import type { StoredAuth } from './authStorage'

export type AuthContextValue = {
  auth: StoredAuth | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

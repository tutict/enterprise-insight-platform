import {
  clearStoredAuth,
  getStoredAuth,
  isAuthExpired,
} from '../auth/authStorage'
import { emitNotification } from '../state/notifications'

export type ApiResponse<T> = {
  code: string
  success: boolean
  message: string
  data: T
  timestamp: string
}

type ApiError = Error & { status?: number }

type RequestOptions = Omit<RequestInit, 'body'> & { body?: unknown }

const DEFAULT_BASE_URL = ''

export const getApiBaseUrl = () => {
  const raw = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? ''
  if (!raw) {
    return DEFAULT_BASE_URL
  }
  return raw.replace(/\/$/, '')
}

const getAuthToken = () => {
  const stored = getStoredAuth()
  if (!stored) {
    return null
  }
  if (isAuthExpired(stored)) {
    clearStoredAuth()
    return null
  }
  return stored.token
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}) {
  const baseUrl = getApiBaseUrl()
  const headers = new Headers(options.headers)

  if (!headers.has('Content-Type') && options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getAuthToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  const text = await response.text()
  const payload = text ? (JSON.parse(text) as ApiResponse<T> | T) : null

  if (!response.ok) {
    if (response.status === 401) {
      clearStoredAuth()
      emitNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'Session expired. Please sign in again.',
      })
    }
    if (response.status === 403) {
      emitNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'You do not have permission to access this resource.',
      })
    }
    const message =
      payload && typeof payload === 'object' && 'message' in payload
        ? String((payload as ApiResponse<T>).message)
        : `HTTP ${response.status}`
    const error = new Error(message) as ApiError
    error.status = response.status
    throw error
  }

  if (payload && typeof payload === 'object' && 'success' in payload) {
    const apiPayload = payload as ApiResponse<T>
    if (!apiPayload.success) {
      throw new Error(apiPayload.message || 'Request failed')
    }
    return apiPayload.data
  }

  return payload as T
}

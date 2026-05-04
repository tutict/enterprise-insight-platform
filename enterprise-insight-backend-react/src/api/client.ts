import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { clearStoredAuth, getStoredAuth, isAuthExpired } from '../features/auth/context/authStorage'

export type ApiResponse<T> = {
  code: string
  success: boolean
  message: string
  data: T
  timestamp: string
}

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

export const httpClient = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 180000,
})

httpClient.interceptors.request.use((config) => {
  const token = getAuthToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export async function apiRequest<T>(
  path: string,
  config: AxiosRequestConfig & { body?: unknown } = {},
) {
  try {
    const { body, ...axiosConfig } = config
    const response = await httpClient.request<ApiResponse<T> | T>({
      url: path,
      ...axiosConfig,
      data: body ?? axiosConfig.data,
    })
    const payload = response.data

    if (payload && typeof payload === 'object' && 'success' in payload) {
      const apiPayload = payload as ApiResponse<T>
      if (!apiPayload.success) {
        throw new Error(apiPayload.message || 'Request failed')
      }
      return apiPayload.data
    }

    return payload as T
  } catch (err) {
    const error = err as AxiosError<ApiResponse<unknown> | { error?: string; message?: string }>
    if (error.response?.status === 401) {
      clearStoredAuth()
    }

    const data = error.response?.data
    const message =
      data && typeof data === 'object' && 'message' in data
        ? String(data.message)
        : data && typeof data === 'object' && 'error' in data
          ? String(data.error)
          : error.message || 'Request failed'

    throw new Error(message)
  }
}

export async function apiHealthCheck() {
  try {
    await httpClient.get('/actuator/health', { timeout: 5000 })
    return true
  } catch {
    try {
      await httpClient.get('/api/metadata/templates', { timeout: 5000 })
      return true
    } catch {
      return false
    }
  }
}

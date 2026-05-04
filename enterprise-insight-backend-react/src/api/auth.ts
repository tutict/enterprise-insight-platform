import { apiRequest } from './client'

export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  token: string
  expiresAt: string
  roles: string[]
}

export function loginUser(request: LoginRequest) {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: request,
  })
}

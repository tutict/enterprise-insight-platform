import { apiRequest } from '../client'
import type { LoginRequest, LoginResponse } from '../types/auth.types'

export function loginUser(request: LoginRequest) {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: request,
  })
}

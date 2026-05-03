import { apiRequest } from './client'
import type { CompileResponse } from './types'

export function compileDsl(dsl: string) {
  return apiRequest<CompileResponse>('/api/compiler/compile', {
    method: 'POST',
    body: {
      requirement: dsl,
    },
  })
}

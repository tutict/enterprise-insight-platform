import { apiRequest } from '../client'
import type { CompileResponse } from '../types/compiler.types'
import type { GraphDefinition } from '../types/graph.types'

export function compileDsl(dsl: string) {
  return apiRequest<CompileResponse>('/api/compiler/compile', {
    method: 'POST',
    body: {
      requirement: dsl,
    },
  })
}

export function compilePromptFromGraph(graph: GraphDefinition) {
  return apiRequest<CompileResponse>('/api/compiler/from-graph', {
    method: 'POST',
    body: graph,
  })
}

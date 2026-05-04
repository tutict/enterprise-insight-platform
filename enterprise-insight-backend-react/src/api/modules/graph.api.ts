import { apiRequest, getApiBaseUrl } from '../client'
import type { GraphCompileResult, GraphDefinition, GraphRunRequest, GraphRunStartResponse } from '../types/graph.types'

export function getGraphStreamUrl(runId: string, lastEventId?: string) {
  const baseUrl = getApiBaseUrl()
  const path = `/api/graph/run/stream/${encodeURIComponent(runId)}`
  const url = new URL(`${baseUrl}${path}`, window.location.origin)

  if (lastEventId) {
    url.searchParams.set('lastEventId', lastEventId)
  }

  return url.toString()
}

export function startGraphRun(request: GraphRunRequest = {}) {
  return apiRequest<GraphRunStartResponse>('/api/graph/run', {
    method: 'POST',
    body: request,
  })
}

export function compileGraph(graph: GraphDefinition) {
  return apiRequest<GraphCompileResult>('/api/graph/compile', {
    method: 'POST',
    body: graph,
  })
}

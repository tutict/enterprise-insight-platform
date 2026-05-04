export type GraphNodeStatus = 'idle' | 'running' | 'success' | 'fail'

export type GraphRunStatus = 'idle' | 'running' | 'failed' | 'completed'

export type GraphConnectionState = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

export type BuilderNodeType = 'start' | 'llm' | 'tool' | 'condition' | 'end'

export type GraphNodeDefinition = {
  id: string
  label: string
  type: BuilderNodeType | string
  config?: Record<string, unknown>
}

export type GraphEdgeDefinition = {
  id: string
  source: string
  target: string
  condition?: 'always' | 'success' | 'failed' | string
  maxIterations?: number
  label?: string
}

export type GraphDefinition = {
  id: string
  name: string
  startNodeId: string
  maxIterations: number
  nodes: GraphNodeDefinition[]
  edges: GraphEdgeDefinition[]
  metadata?: Record<string, unknown>
}

export type GraphEventType =
  | 'GRAPH_RUN_STARTED'
  | 'NODE_STARTED'
  | 'NODE_SUCCEEDED'
  | 'NODE_FAILED'
  | 'EDGE_TRAVERSED'
  | 'GRAPH_RUN_COMPLETED'
  | 'GRAPH_RUN_FAILED'
  | 'STREAM_CONNECTING'
  | 'STREAM_CONNECTED'
  | 'STREAM_RECONNECTING'
  | 'STREAM_DISCONNECTED'

export type GraphEvent = {
  runId: string
  type: GraphEventType
  nodeId?: string
  edgeId?: string
  timestamp?: number | string
  eventId?: string
  payload?: Record<string, unknown>
}

export type GraphRunRequest = {
  runId?: string
  maxIterations?: number
  requiredRepairIterations?: number
  graph?: GraphDefinition
}

export type GraphRunStartResponse = {
  runId: string
  graph: GraphDefinition
}

export type GraphCompileResult = {
  valid: boolean
  graph: GraphDefinition
  errors: string[]
  warnings: string[]
}

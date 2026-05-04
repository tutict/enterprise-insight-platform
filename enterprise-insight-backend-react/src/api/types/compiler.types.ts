export type DslFlowEdge = {
  target: string
  condition?: string
  label?: string
  maxIterations?: number
}

export type DslFlowStep = {
  id: string
  label: string
  type: string
  config?: Record<string, unknown>
  next?: DslFlowEdge[]
}

export type DslModel = {
  name: string
  type: string
  role?: string
  goal?: string
  task?: string
  requirement: string
  modules: string[]
  flow?: DslFlowStep[]
  constraints: Record<string, string>
  metadata?: Record<string, unknown>
  outputFormat: string
}

export type CompileResponse = {
  dsl: DslModel
  prompt: string
}

export type ProjectSummary = {
  scannedFiles: number
  moduleCount: number
  apiEndpointCount: number
  frontendRouteCount: number
  businessCapabilityCount: number
  documentCount: number
  testCount: number
}

export type ProjectModule = {
  name: string
  path: string
  type: string
  fileCount: number
  markers: string[]
}

export type ApiEndpointEvidence = {
  method: string
  path: string
  sourcePath: string
  line: number
}

export type FrontendRouteEvidence = {
  path: string
  sourcePath: string
  line: number
}

export type CodeEvidence = {
  kind: string
  name: string
  sourcePath: string
  line: number
}

export type BusinessCapability = {
  name: string
  category: string
  evidenceCount: number
  evidence: CodeEvidence[]
}

export type DeliveryOpportunity = {
  priority: string
  title: string
  rationale: string
  evidence: CodeEvidence[]
}

export type ProjectInventory = {
  rootPath: string
  generatedAt: string
  summary: ProjectSummary
  modules: ProjectModule[]
  apiEndpoints: ApiEndpointEvidence[]
  frontendRoutes: FrontendRouteEvidence[]
  businessCapabilities: BusinessCapability[]
  documents: CodeEvidence[]
  tests: CodeEvidence[]
  deliveryOpportunities: DeliveryOpportunity[]
}

export type ProjectDeliveryBrief = {
  title: string
  summary: string
  requirement: string
  playbookId: string
  playbookName: string
  targetDirectory: string
  verifyCommands: string[][]
  maxRepairRounds: number
  options: Record<string, unknown>
  evidence: CodeEvidence[]
  generatedAt: string
}

import type { DslModel } from './compiler.types'

export type GeneratedProjectFile = {
  relativePath: string
  absolutePath: string
  bytesWritten: number
}

export type VerificationCommandResult = {
  command: string
  exitCode: number
  timedOut: boolean
  stdout: string
  stderr: string
  durationMillis: number
}

export type VerificationResult = {
  successful: boolean
  summary: string
  commandResults: VerificationCommandResult[]
}

export type AutoRepairAttempt = {
  attemptNumber: number
  successful: boolean
  prompt: string
  generatedOutput: string
  writtenFiles: GeneratedProjectFile[]
  verificationResult: VerificationResult | null
}

export type AutoRepairGenerationResponse = {
  successful: boolean
  status: string
  projectRoot: string | null
  totalAttempts: number
  finalOutput: string
  finalVerificationResult: VerificationResult | null
  attempts: AutoRepairAttempt[]
}

export type OrchestratorRunRequest = {
  runId?: string
  requirement: string
  model?: string
  targetDirectory: string
  verifyCommands: string[][]
  maxRepairRounds: number
  options?: Record<string, unknown>
}

export type OrchestratorRunResponse = {
  runId: string
  dsl: DslModel
  harnessPrompt: string
  generation: AutoRepairGenerationResponse
  createdAt: string
}

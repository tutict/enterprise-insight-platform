export type RunConfig = {
  model: string
  targetDirectory: string
  verifyCommand: string
  maxRepairRounds: number
}

export type RunRuntimeInput = {
  dsl: string
  runId?: string
}

export type RunEngineConfig = RunConfig & Partial<RunRuntimeInput>

export const DEFAULT_VERIFY_COMMAND = 'mvn test'

export const splitVerifyCommand = (value: string) =>
  value
    .split(' ')
    .map((part) => part.trim())
    .filter(Boolean)

export const normalizeRunConfig = (config: RunEngineConfig): RunEngineConfig => ({
  ...config,
  model: config.model.trim(),
  targetDirectory: config.targetDirectory.trim(),
  verifyCommand: config.verifyCommand.trim() || DEFAULT_VERIFY_COMMAND,
  maxRepairRounds: Math.max(0, config.maxRepairRounds),
  dsl: config.dsl?.trim(),
})

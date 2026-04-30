import { apiRequest } from './client'

export type DslDocument = {
  project: {
    type: string
    modules: string[]
  }
  constraints: {
    db: string
    [key: string]: unknown
  }
}

export type CompiledPrompt = {
  templateName: string
  harnessPrompt: string
  sections: string[]
  compiledAt: string
}

export type PromptCompileResponse = {
  dslDocument: DslDocument
  compiledPrompt: CompiledPrompt
}

export type AutoRepairGenerationResponse = {
  successful: boolean
  status: string
  projectRoot: string | null
  totalAttempts: number
  finalOutput: string
}

export function compileHarnessPrompt(dsl: string) {
  return apiRequest<PromptCompileResponse>('/api/prompt-compiler/compile', {
    method: 'POST',
    body: {
      dsl,
      templateName: 'harness-default',
    },
  })
}

export function generateCodeWithAutoRepair(params: {
  model: string
  prompt: string
  targetDirectory: string
}) {
  return apiRequest<AutoRepairGenerationResponse>(
    '/api/agent-adapter/auto-repair/generate',
    {
      method: 'POST',
      body: {
        model: params.model,
        prompt: params.prompt,
        targetDirectory: params.targetDirectory,
        maxRepairRounds: 2,
        verifyCommands: [['mvn', 'test']],
      },
    },
  )
}

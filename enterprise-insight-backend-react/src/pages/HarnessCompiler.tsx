import { useMemo, useState } from 'react'
import {
  type AutoRepairGenerationResponse,
  compileHarnessPrompt,
  generateCodeWithAutoRepair,
} from '../api/harness'

const defaultRequirement =
  'Build a Spring Boot application with user, auth, and leaderboard modules using MySQL.'

const moduleKeywords = ['user', 'auth', 'leaderboard', 'order', 'payment', 'notification']

function buildDsl(requirement: string) {
  const normalized = requirement.toLowerCase()
  const modules = moduleKeywords.filter((keyword) => normalized.includes(keyword))
  const resolvedModules = modules.length > 0 ? modules : ['user', 'auth']
  const db = normalized.includes('postgres')
    ? 'postgresql'
    : normalized.includes('mongo')
      ? 'mongodb'
      : 'mysql'

  return {
    project: {
      type: 'spring_boot',
      modules: resolvedModules,
    },
    constraints: {
      db,
    },
  }
}

function toYaml(dsl: ReturnType<typeof buildDsl>) {
  return [
    'project:',
    `  type: ${dsl.project.type}`,
    '  modules:',
    ...dsl.project.modules.map((module) => `    - ${module}`),
    'constraints:',
    `  db: ${dsl.constraints.db}`,
  ].join('\n')
}

function buildPromptPreview(dsl: ReturnType<typeof buildDsl>) {
  return [
    '# ROLE',
    'You are an AI Harness coding agent that generates production-ready Java and Spring Boot code.',
    '',
    '# GOAL',
    'Convert the structured DSL into a complete, compilable implementation plan and code files.',
    '',
    '# MODULES',
    `- project_type: ${dsl.project.type}`,
    ...dsl.project.modules.map((module) => `- module: ${module}`),
    '',
    '# CONSTRAINTS',
    `- db: ${dsl.constraints.db}`,
    '',
    '# OUTPUT FORMAT',
    'Return only generated files using this exact format:',
    '===FILE START===',
    'relative/path/from/project/root',
    'complete file content',
    '===FILE END===',
  ].join('\n')
}

function HarnessCompiler() {
  const [requirement, setRequirement] = useState(defaultRequirement)
  const [model, setModel] = useState('llama3.1')
  const [targetDirectory, setTargetDirectory] = useState('generated-harness-app')
  const [prompt, setPrompt] = useState('')
  const [result, setResult] = useState<AutoRepairGenerationResponse | null>(null)
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState('')

  const dsl = useMemo(() => buildDsl(requirement), [requirement])
  const dslYaml = useMemo(() => toYaml(dsl), [dsl])
  const promptPreview = prompt || buildPromptPreview(dsl)

  const handleGenerate = async () => {
    setIsGenerating(true)
    setError('')
    setResult(null)
    try {
      const compiled = await compileHarnessPrompt(dslYaml)
      const compiledPrompt = compiled.compiledPrompt.harnessPrompt
      setPrompt(compiledPrompt)
      const generated = await generateCodeWithAutoRepair({
        model,
        prompt: compiledPrompt,
        targetDirectory,
      })
      setResult(generated)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Code generation failed')
    } finally {
      setIsGenerating(false)
    }
  }

  return (
    <div className="harness-page">
      <section className="glass-card harness-workspace">
        <div className="harness-copy">
          <p className="eyebrow">AI Harness Compiler</p>
          <h2>Requirement to runnable code.</h2>
          <p className="muted">
            Draft a requirement, inspect the DSL and Prompt, then start code generation.
          </p>
        </div>

        <div className="harness-form">
          <label>
            Requirement
            <textarea
              value={requirement}
              onChange={(event) => {
                setRequirement(event.target.value)
                setPrompt('')
              }}
              rows={6}
            />
          </label>

          <div className="harness-fields">
            <label>
              Model
              <input
                value={model}
                onChange={(event) => setModel(event.target.value)}
              />
            </label>
            <label>
              Target directory
              <input
                value={targetDirectory}
                onChange={(event) => setTargetDirectory(event.target.value)}
              />
            </label>
          </div>

          <button
            className="primary-btn"
            type="button"
            onClick={handleGenerate}
            disabled={isGenerating || requirement.trim().length === 0}
          >
            {isGenerating ? 'Generating...' : '生成代码'}
          </button>
        </div>
      </section>

      <section className="harness-output-grid">
        <div className="glass-card harness-panel">
          <div className="card-header">
            <h3>DSL Structure</h3>
            <span className="chip">{dsl.project.type}</span>
          </div>
          <pre>{dslYaml}</pre>
        </div>

        <div className="glass-card harness-panel">
          <div className="card-header">
            <h3>Harness Prompt</h3>
            <span className="chip">{prompt ? 'compiled' : 'preview'}</span>
          </div>
          <pre>{promptPreview}</pre>
        </div>
      </section>

      {(result || error) && (
        <section className="glass-card harness-result">
          <h3>Generation Result</h3>
          {error ? (
            <p className="form-error">{error}</p>
          ) : (
            <div className="result-grid">
              <span>Status</span>
              <strong>{result?.status}</strong>
              <span>Attempts</span>
              <strong>{result?.totalAttempts}</strong>
              <span>Project root</span>
              <strong>{result?.projectRoot ?? 'Not written'}</strong>
            </div>
          )}
        </section>
      )}
    </div>
  )
}

export default HarnessCompiler

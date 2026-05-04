import CodeBlock from '../../../shared/components/CodeBlock'
import type { CompileResponse } from '../../../api/types/compiler.types'
import type { AsyncStatus } from '../store/dslStore'
import YamlEditor from './YamlEditor'

type DslEditorWorkspaceProps = {
  name: string
  setName: (value: string) => void
  dslText: string
  setDslText: (value: string) => void
  compile: () => void
  save: () => void
  compilerState: {
    prompt: string
    status: AsyncStatus
    isCompiling: boolean
    error: string
    result: CompileResponse | null
  }
}

export default function DslEditorWorkspace({
  name,
  setName,
  dslText,
  setDslText,
  compile,
  save,
  compilerState,
}: DslEditorWorkspaceProps) {
  return (
    <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(360px,0.85fr)]">
      <section className="panel p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">DSL Editor</h2>
            <p className="muted">POST /api/compiler/compile</p>
          </div>
          <button
            className="btn-primary"
            type="button"
            onClick={compile}
            disabled={compilerState.isCompiling}
          >
            {compilerState.isCompiling ? 'Compiling...' : 'Compile'}
          </button>
        </div>

        <YamlEditor value={dslText} onChange={setDslText} minHeight="540px" />

        <div className="mt-4 flex flex-wrap items-center gap-3">
          <input
            className="field w-72"
            value={name}
            placeholder="DSL name"
            onChange={(event) => setName(event.target.value)}
          />
          <button
            className="btn-secondary"
            type="button"
            onClick={save}
          >
            Save DSL
          </button>
        </div>

        {compilerState.error ? (
          <div className="mt-4 rounded-lg border border-red-400/30 bg-red-950/60 p-3 text-sm text-red-100">
            <p className="font-medium">Compile failed</p>
            <p className="mt-1 text-red-100/80">{compilerState.error}</p>
          </div>
        ) : null}
      </section>

      <section className="space-y-5">
        <div className="panel p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-100">Compiled Prompt</h3>
            <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
              {compilerState.status === 'success' ? 'compiled' : 'idle'}
            </span>
          </div>
          <CodeBlock value={compilerState.prompt} emptyLabel="Compile a DSL to inspect the returned prompt." collapsible />
        </div>

        <div className="panel p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-100">Compiler Response</h3>
          <CodeBlock
            value={compilerState.result ? JSON.stringify(compilerState.result.dsl, null, 2) : ''}
            emptyLabel="No compiler response yet."
            collapsible
          />
        </div>
      </section>
    </div>
  )
}

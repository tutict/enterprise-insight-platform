import { useTranslation } from 'react-i18next'
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
  const { t } = useTranslation('dsl')

  return (
    <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(360px,0.85fr)]">
      <section className="panel p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">{t('editor.title')}</h2>
            <p className="muted">POST /api/compiler/compile</p>
          </div>
          <button
            className="btn-primary"
            type="button"
            onClick={compile}
            disabled={compilerState.isCompiling}
          >
            {compilerState.isCompiling ? t('editor.compiling') : t('editor.compile')}
          </button>
        </div>

        <YamlEditor value={dslText} onChange={setDslText} minHeight="540px" />

        <div className="mt-4 flex flex-wrap items-center gap-3">
          <input
            className="field w-72"
            value={name}
            placeholder={t('editor.namePlaceholder')}
            onChange={(event) => setName(event.target.value)}
          />
          <button
            className="btn-secondary"
            type="button"
            onClick={save}
          >
            {t('editor.save')}
          </button>
        </div>

        {compilerState.error ? (
          <div className="mt-4 rounded-lg border border-red-400/30 bg-red-950/60 p-3 text-sm text-red-100">
            <p className="font-medium">{t('editor.failed')}</p>
            <p className="mt-1 text-red-100/80">{compilerState.error}</p>
          </div>
        ) : null}
      </section>

      <section className="space-y-5">
        <div className="panel p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-100">{t('prompt.title')}</h3>
            <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
              {compilerState.status === 'success' ? t('prompt.compiled') : t('prompt.idle')}
            </span>
          </div>
          <CodeBlock value={compilerState.prompt} emptyLabel={t('prompt.empty')} collapsible />
        </div>

        <div className="panel p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-100">{t('response.title')}</h3>
          <CodeBlock
            value={compilerState.result ? JSON.stringify(compilerState.result.dsl, null, 2) : ''}
            emptyLabel={t('response.empty')}
            collapsible
          />
        </div>
      </section>
    </div>
  )
}

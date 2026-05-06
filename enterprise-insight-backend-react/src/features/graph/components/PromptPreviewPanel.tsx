import { useTranslation } from 'react-i18next'

type PromptPreviewPanelProps = {
  prompt: string
  setPrompt: (value: string) => void
  copyPrompt: () => void
}

export default function PromptPreviewPanel({ prompt, setPrompt, copyPrompt }: PromptPreviewPanelProps) {
  const { t } = useTranslation('common')

  return (
    <section className="panel p-5">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-slate-100">{t('graph.prompt.title')}</h3>
          <p className="muted">{t('graph.prompt.description')}</p>
        </div>
        <button className="btn-secondary px-3 py-1.5 text-xs" type="button" onClick={copyPrompt}>
          {t('clipboard.copy')}
        </button>
      </div>
      <textarea
        className="field min-h-[320px] w-full resize-y leading-6"
        value={prompt}
        placeholder={t('graph.prompt.placeholder')}
        onChange={(event) => setPrompt(event.target.value)}
      />
    </section>
  )
}

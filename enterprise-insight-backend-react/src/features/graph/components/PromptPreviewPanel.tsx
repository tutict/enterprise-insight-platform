type PromptPreviewPanelProps = {
  prompt: string
  setPrompt: (value: string) => void
  copyPrompt: () => void
}

export default function PromptPreviewPanel({ prompt, setPrompt, copyPrompt }: PromptPreviewPanelProps) {
  return (
    <section className="panel p-5">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-slate-100">Generated Prompt</h3>
          <p className="muted">Graph -&gt; DSL -&gt; template prompt output.</p>
        </div>
        <button className="btn-secondary px-3 py-1.5 text-xs" type="button" onClick={copyPrompt}>
          Copy
        </button>
      </div>
      <textarea
        className="field min-h-[320px] w-full resize-y leading-6"
        value={prompt}
        placeholder="Click Generate Prompt to compile the current graph."
        onChange={(event) => setPrompt(event.target.value)}
      />
    </section>
  )
}

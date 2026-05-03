import { useMemo, useState } from 'react'
import CodeBlock from './CodeBlock'

type GeneratedFile = {
  id: string
  path: string
  code: string
}

type CodeOutputProps = {
  value: string
  emptyLabel?: string
}

const parseGeneratedFiles = (value: string): GeneratedFile[] => {
  const files: GeneratedFile[] = []
  const blockPattern = /===FILE START===([\s\S]*?)===FILE END===/g
  let match: RegExpExecArray | null
  let index = 0

  while ((match = blockPattern.exec(value)) !== null) {
    const block = match[1].replace(/^\s*\r?\n/, '').replace(/\s*$/, '')
    const lines = block.split(/\r?\n/)
    const path = lines.shift()?.trim() || `generated-file-${index + 1}`
    files.push({
      id: `${index}-${path}`,
      path,
      code: lines.join('\n').trim(),
    })
    index += 1
  }

  return files
}

export default function CodeOutput({ value, emptyLabel = 'No output yet.' }: CodeOutputProps) {
  const files = useMemo(() => parseGeneratedFiles(value), [value])
  const [openFiles, setOpenFiles] = useState<Record<string, boolean>>({})

  if (!files.length) {
    return <CodeBlock value={value} emptyLabel={emptyLabel} collapsible />
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between rounded-md border border-white/10 bg-console-950 px-3 py-2">
        <span className="text-xs font-medium text-slate-300">{files.length} generated files</span>
        <span className="text-xs text-slate-500">expand a file to inspect or copy</span>
      </div>

      {files.map((file, index) => {
        const isOpen = openFiles[file.id] ?? index === 0
        return (
          <div key={file.id} className="overflow-hidden rounded-lg border border-white/10 bg-console-950">
            <button
              type="button"
              className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left transition hover:bg-white/[0.04]"
              aria-expanded={isOpen}
              onClick={() =>
                setOpenFiles((current) => ({
                  ...current,
                  [file.id]: !isOpen,
                }))
              }
            >
              <span className="min-w-0 truncate text-sm font-medium text-slate-100">{file.path}</span>
              <span className="shrink-0 rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
                {isOpen ? 'Collapse' : 'Expand'}
              </span>
            </button>
            {isOpen ? (
              <div className="border-t border-white/10 p-3">
                <CodeBlock value={file.code} emptyLabel="File output is empty." collapsible />
              </div>
            ) : null}
          </div>
        )
      })}
    </div>
  )
}

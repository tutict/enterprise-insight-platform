import { useMemo, useState } from 'react'
import { useNotificationStore } from '../../../store/uiStore'

type YamlEditorProps = {
  value: string
  onChange: (value: string) => void
  minHeight?: string
  placeholder?: string
}

const renderScalar = (value: string) => {
  if (!value) {
    return null
  }
  const trimmed = value.trim()
  const className =
    /^['"].*['"]$/.test(trimmed)
      ? 'text-amber-200'
      : /^(true|false|null)$/i.test(trimmed)
        ? 'text-violet-200'
        : /^-?\d+(\.\d+)?$/.test(trimmed)
          ? 'text-sky-200'
          : 'text-slate-200'

  return <span className={className}>{value}</span>
}

const renderLine = (line: string) => {
  if (!line) {
    return <span>&nbsp;</span>
  }

  const commentIndex = line.indexOf('#')
  const body = commentIndex >= 0 ? line.slice(0, commentIndex) : line
  const comment = commentIndex >= 0 ? line.slice(commentIndex) : ''
  const keyMatch = body.match(/^(\s*(?:-\s*)?)([A-Za-z0-9_.-]+)(\s*:)(.*)$/)

  if (!keyMatch) {
    return (
      <>
        <span className="text-slate-200">{body}</span>
        {comment ? <span className="text-slate-500">{comment}</span> : null}
      </>
    )
  }

  const [, prefix, key, separator, scalar] = keyMatch

  return (
    <>
      <span className="text-slate-400">{prefix}</span>
      <span className="text-teal-200">{key}</span>
      <span className="text-slate-500">{separator}</span>
      {renderScalar(scalar)}
      {comment ? <span className="text-slate-500">{comment}</span> : null}
    </>
  )
}

export default function YamlEditor({
  value,
  onChange,
  minHeight = '420px',
  placeholder = 'Enter YAML DSL...',
}: YamlEditorProps) {
  const [scroll, setScroll] = useState({ left: 0, top: 0 })
  const pushNotification = useNotificationStore((state) => state.push)
  const lines = useMemo(() => value.split('\n'), [value])
  const lineNumbers = useMemo(
    () => Array.from({ length: Math.max(lines.length, 1) }, (_, index) => index + 1).join('\n'),
    [lines.length],
  )
  const editorStats = useMemo(
    () => ({
      lines: Math.max(lines.length, 1),
      chars: value.length,
    }),
    [lines.length, value.length],
  )

  const copyDsl = async () => {
    if (!value.trim()) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'DSL editor is empty.',
      })
      return
    }

    try {
      await navigator.clipboard.writeText(value)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: 'DSL copied to clipboard.',
      })
    } catch {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'Copy failed. Check browser clipboard permission.',
      })
    }
  }

  return (
    <div
      className="overflow-hidden rounded-lg border border-white/10 bg-console-950 shadow-inner focus-within:border-teal-400/60 focus-within:ring-2 focus-within:ring-teal-400/10"
    >
      <div className="flex items-center justify-between gap-3 border-b border-white/10 bg-console-900/80 px-3 py-2 pl-[4.75rem]">
        <span className="text-xs font-medium text-slate-400">YAML editor</span>
        <button className="btn-secondary px-2 py-1 text-xs" type="button" onClick={() => void copyDsl()}>
          Copy DSL
        </button>
      </div>
      <div
        className="relative overflow-hidden"
        style={{ minHeight }}
      >
        <div className="pointer-events-none absolute inset-y-0 left-0 z-20 w-14 border-r border-white/10 bg-console-900/80 px-3 py-4 text-right text-xs leading-6 text-slate-600">
          <pre style={{ transform: `translateY(-${scroll.top}px)` }}>{lineNumbers}</pre>
        </div>
        <pre
          aria-hidden="true"
          className="pointer-events-none absolute inset-0 z-0 min-w-max overflow-hidden py-4 pb-12 pl-[4.75rem] pr-4 text-xs leading-6"
          style={{ transform: `translate(${-scroll.left}px, ${-scroll.top}px)` }}
        >
          {lines.map((line, index) => (
            <span key={`${index}-${line}`}>
              {renderLine(line)}
              {index < lines.length - 1 ? '\n' : null}
            </span>
          ))}
        </pre>
        <textarea
          className="relative z-10 block w-full resize-y overflow-auto whitespace-pre bg-transparent py-4 pb-12 pl-[4.75rem] pr-4 text-xs leading-6 text-transparent caret-teal-300 outline-none selection:bg-teal-400/25 placeholder:text-slate-600"
          style={{ minHeight }}
          value={value}
          placeholder={placeholder}
          spellCheck={false}
          wrap="off"
          onChange={(event) => onChange(event.target.value)}
          onScroll={(event) =>
            setScroll({
              left: event.currentTarget.scrollLeft,
              top: event.currentTarget.scrollTop,
            })
          }
        />
        <div className="pointer-events-none absolute inset-x-0 bottom-0 z-20 flex items-center justify-between border-t border-white/10 bg-console-900/90 px-3 py-2 pl-[4.75rem] text-xs text-slate-500">
          <span>highlighting enabled</span>
          <span>{editorStats.lines} lines / {editorStats.chars.toLocaleString()} chars</span>
        </div>
      </div>
    </div>
  )
}

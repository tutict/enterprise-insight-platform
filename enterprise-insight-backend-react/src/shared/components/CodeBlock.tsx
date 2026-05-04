import { useState } from 'react'
import { useNotificationStore } from '../../store/uiStore'

type CodeBlockProps = {
  value: string
  emptyLabel?: string
  className?: string
  collapsible?: boolean
  defaultCollapsed?: boolean
}

export default function CodeBlock({
  value,
  emptyLabel = 'No output yet.',
  className = '',
  collapsible = false,
  defaultCollapsed = false,
}: CodeBlockProps) {
  const [collapsed, setCollapsed] = useState(defaultCollapsed)
  const pushNotification = useNotificationStore((state) => state.push)
  const displayValue = value || emptyLabel

  const copyToClipboard = async () => {
    if (!value) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'Nothing to copy yet.',
      })
      return
    }

    try {
      await navigator.clipboard.writeText(value)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: 'Copied to clipboard.',
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
    <div className="overflow-hidden rounded-lg border border-white/10 bg-console-950">
      <div className="flex items-center justify-between gap-3 border-b border-white/10 px-3 py-2">
        <span className="text-xs text-slate-500">{value ? `${value.length.toLocaleString()} chars` : 'empty'}</span>
        <div className="flex items-center gap-2">
          {collapsible ? (
            <button
              className="btn-secondary px-2 py-1 text-xs"
              type="button"
              aria-expanded={!collapsed}
              onClick={() => setCollapsed((current) => !current)}
            >
              {collapsed ? 'Expand' : 'Collapse'}
            </button>
          ) : null}
          <button className="btn-secondary px-2 py-1 text-xs" type="button" onClick={() => void copyToClipboard()}>
            Copy
          </button>
        </div>
      </div>
      {collapsed ? null : (
        <pre className={`max-h-[520px] overflow-auto p-4 text-xs leading-6 text-slate-200 ${className}`}>
          {displayValue}
        </pre>
      )}
    </div>
  )
}

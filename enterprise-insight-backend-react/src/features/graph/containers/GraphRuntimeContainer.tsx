import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { compilePromptFromGraph } from '../../../api/modules/compiler.api'
import { listPlaybookTemplates } from '../../../api/modules/graph.api'
import type { PlaybookTemplate } from '../../../api/types/graph.types'
import { useNotificationStore } from '../../../store/uiStore'
import { useGraphRuntime } from '../hooks/useGraphRuntime'
import { useGraphBuilderStore } from '../store/graphBuilderStore'
import { useGraphStore } from '../store/graphStore'
import GraphRuntimeWorkspace from '../components/GraphRuntimeWorkspace'

export default function GraphRuntimeContainer() {
  const { t } = useTranslation('common')
  const [generatedPrompt, setGeneratedPrompt] = useState('')
  const [isGeneratingPrompt, setIsGeneratingPrompt] = useState(false)
  const [playbooks, setPlaybooks] = useState<PlaybookTemplate[]>([])
  const [selectedPlaybookId, setSelectedPlaybookId] = useState('')
  const runtime = useGraphRuntime()
  const status = useGraphStore((state) => state.status)
  const connectionState = useGraphStore((state) => state.connectionState)
  const runId = useGraphStore((state) => state.runId)
  const lastEventId = useGraphStore((state) => state.lastEventId)
  const eventCount = useGraphStore((state) => state.eventLog.length)
  const toGraphDefinition = useGraphBuilderStore((state) => state.toGraphDefinition)
  const loadGraph = useGraphBuilderStore((state) => state.loadGraph)
  const validation = useGraphBuilderStore((state) => state.validation)
  const pushNotification = useNotificationStore((state) => state.push)
  const selectedPlaybook = useMemo(
    () => playbooks.find((playbook) => playbook.id === selectedPlaybookId) ?? null,
    [playbooks, selectedPlaybookId],
  )

  useEffect(() => {
    let cancelled = false

    const loadPlaybooks = async () => {
      try {
        const loaded = await listPlaybookTemplates()
        if (cancelled) {
          return
        }
        setPlaybooks(loaded)
        const defaultPlaybook = loaded[0]
        if (defaultPlaybook) {
          setSelectedPlaybookId(defaultPlaybook.id)
          loadGraph(defaultPlaybook.graph)
        }
      } catch (err) {
        if (!cancelled) {
          pushNotification({
            id: crypto.randomUUID(),
            type: 'error',
            message: err instanceof Error ? err.message : t('graph.notifications.playbookLoadFailed'),
          })
        }
      }
    }

    void loadPlaybooks()

    return () => {
      cancelled = true
    }
  }, [loadGraph, pushNotification, t])

  const compileCurrentGraph = async () => {
    try {
      const result = await runtime.compile(toGraphDefinition())
      pushNotification({
        id: crypto.randomUUID(),
        type: result.valid ? 'success' : 'error',
        message: result.valid ? t('graph.notifications.compiled') : result.errors[0] ?? t('graph.notifications.validationFailed'),
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('graph.notifications.compileFailed'),
      })
    }
  }

  const generatePromptFromCurrentGraph = async () => {
    if (!validation.valid) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: validation.errors[0] ?? t('graph.notifications.invalid'),
      })
      return
    }

    setIsGeneratingPrompt(true)
    try {
      const response = await compilePromptFromGraph(toGraphDefinition())
      setGeneratedPrompt(response.prompt)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: t('graph.notifications.promptGenerated'),
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('graph.notifications.promptFailed'),
      })
    } finally {
      setIsGeneratingPrompt(false)
    }
  }

  const copyGeneratedPrompt = async () => {
    if (!generatedPrompt) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('graph.notifications.noPromptToCopy'),
      })
      return
    }

    try {
      await navigator.clipboard.writeText(generatedPrompt)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: t('graph.notifications.promptCopied'),
      })
    } catch {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('clipboard.copyFailed'),
      })
    }
  }

  const applySelectedPlaybook = () => {
    if (!selectedPlaybook) {
      return
    }
    loadGraph(selectedPlaybook.graph)
    setGeneratedPrompt('')
    pushNotification({
      id: crypto.randomUUID(),
      type: 'success',
      message: t('graph.notifications.playbookLoaded', { name: selectedPlaybook.name }),
    })
  }

  const runCurrentGraph = async () => {
    if (!validation.valid) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: validation.errors[0] ?? t('graph.notifications.invalid'),
      })
      return
    }

    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: t('graph.notifications.runRequested'),
    })
    try {
      await runtime.run({
        graph: toGraphDefinition(),
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('graph.notifications.runFailed'),
      })
    }
  }

  return (
    <GraphRuntimeWorkspace
      status={status}
      connectionState={connectionState}
      runId={runId}
      lastEventId={lastEventId}
      eventCount={eventCount}
      playbooks={playbooks}
      selectedPlaybookId={selectedPlaybookId}
      selectedPlaybook={selectedPlaybook}
      setSelectedPlaybookId={setSelectedPlaybookId}
      applySelectedPlaybook={applySelectedPlaybook}
      generatedPrompt={generatedPrompt}
      isGeneratingPrompt={isGeneratingPrompt}
      compileGraph={() => void compileCurrentGraph()}
      generatePrompt={() => void generatePromptFromCurrentGraph()}
      runGraph={() => void runCurrentGraph()}
      setGeneratedPrompt={setGeneratedPrompt}
      copyGeneratedPrompt={() => void copyGeneratedPrompt()}
    />
  )
}

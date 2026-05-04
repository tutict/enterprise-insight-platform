import { useState } from 'react'
import { compilePromptFromGraph } from '../../../api/modules/compiler.api'
import { useNotificationStore } from '../../../store/uiStore'
import { useGraphRuntime } from '../hooks/useGraphRuntime'
import { useGraphBuilderStore } from '../store/graphBuilderStore'
import { useGraphStore } from '../store/graphStore'
import GraphRuntimeWorkspace from '../components/GraphRuntimeWorkspace'

export default function GraphRuntimeContainer() {
  const [generatedPrompt, setGeneratedPrompt] = useState('')
  const [isGeneratingPrompt, setIsGeneratingPrompt] = useState(false)
  const runtime = useGraphRuntime()
  const status = useGraphStore((state) => state.status)
  const connectionState = useGraphStore((state) => state.connectionState)
  const runId = useGraphStore((state) => state.runId)
  const lastEventId = useGraphStore((state) => state.lastEventId)
  const eventCount = useGraphStore((state) => state.eventLog.length)
  const toGraphDefinition = useGraphBuilderStore((state) => state.toGraphDefinition)
  const validation = useGraphBuilderStore((state) => state.validation)
  const pushNotification = useNotificationStore((state) => state.push)

  const compileCurrentGraph = async () => {
    try {
      const result = await runtime.compile(toGraphDefinition())
      pushNotification({
        id: crypto.randomUUID(),
        type: result.valid ? 'success' : 'error',
        message: result.valid ? 'Graph compiled.' : result.errors[0] ?? 'Graph validation failed.',
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : 'Graph compile failed.',
      })
    }
  }

  const generatePromptFromCurrentGraph = async () => {
    if (!validation.valid) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: validation.errors[0] ?? 'Graph is invalid.',
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
        message: 'Prompt generated from graph.',
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : 'Prompt generation failed.',
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
        message: 'No generated prompt to copy.',
      })
      return
    }

    try {
      await navigator.clipboard.writeText(generatedPrompt)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: 'Prompt copied.',
      })
    } catch {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: 'Copy failed. Check browser clipboard permission.',
      })
    }
  }

  const runCurrentGraph = async () => {
    if (!validation.valid) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: validation.errors[0] ?? 'Graph is invalid.',
      })
      return
    }

    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: 'Graph run requested.',
    })
    try {
      await runtime.run({
        graph: toGraphDefinition(),
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : 'Graph run failed.',
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

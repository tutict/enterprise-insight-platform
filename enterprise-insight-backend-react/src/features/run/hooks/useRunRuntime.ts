import { useCallback, useRef } from 'react'
import { runEngine } from '../engine/runEngine'
import type { RunConfig, RunEngineConfig, RunRuntimeInput } from '../model/runConfig'
import type { Execution, RunEvent, StepKey } from '../model/runEvent'
import { sendRunControl, startRunExecutionStream } from '../services/runRuntimeService'
import { useRunStore } from '../store/runStore'

type RunRuntimeResult = {
  execution: Execution | null
  stale: boolean
}

const getErrorMessage = (err: unknown) => (err instanceof Error ? err.message : 'Run failed')

const createRunId = () => {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }

  return `run-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export function useRunRuntime(config: RunConfig) {
  const dispatch = useRunStore((state) => state.dispatch)
  const activeRunIdRef = useRef<string | null>(null)

  const consumeEvent = useCallback(
    (event: RunEvent) => {
      if (event.type === 'RUN_REQUESTED') {
        activeRunIdRef.current = event.runId ?? null
      }

      if (activeRunIdRef.current && event.runId && event.runId !== activeRunIdRef.current) {
        return
      }

      dispatch(event)
    },
    [dispatch],
  )

  const run = useCallback(
    async (runtimeInput: RunRuntimeInput): Promise<RunRuntimeResult> => {
      const requestedRunId = runtimeInput.runId ?? createRunId()
      let runId: string | null = requestedRunId
      const engineConfig: RunEngineConfig = { ...config, ...runtimeInput, runId: requestedRunId }

      try {
        const started = await startRunExecutionStream(engineConfig)
        runId = started.runId
        activeRunIdRef.current = runId

        for await (const event of runEngine(runId)) {
          if (event.type === 'RUN_REQUESTED') {
            runId = event.runId ?? null
          }
          consumeEvent(event)
        }
      } catch (err) {
        consumeEvent({
          type: 'RUN_FAILED',
          runId: runId ?? undefined,
          error: getErrorMessage(err),
        })
      }

      const execution = useRunStore.getState().execution
      if (runId && execution.id !== runId) {
        return {
          execution: null,
          stale: true,
        }
      }

      return {
        execution,
        stale: false,
      }
    },
    [config, consumeEvent],
  )

  const sendControl = useCallback(
    async (type: 'PAUSE' | 'RESUME' | 'CANCEL', explicitRunId?: string) => {
      const runId = explicitRunId ?? activeRunIdRef.current ?? useRunStore.getState().runId
      if (!runId) {
        throw new Error('No active run to control.')
      }
      await sendRunControl({ type, runId })
    },
    [],
  )

  const retryStep = useCallback(async (step: StepKey, explicitRunId?: string) => {
    const runId = explicitRunId ?? activeRunIdRef.current ?? useRunStore.getState().runId
    if (!runId) {
      throw new Error('No active run to retry.')
    }
    await sendRunControl({ type: 'RETRY_STEP', runId, step })
  }, [])

  return {
    run,
    pause: (runId?: string) => sendControl('PAUSE', runId),
    resume: (runId?: string) => sendControl('RESUME', runId),
    cancel: (runId?: string) => sendControl('CANCEL', runId),
    retryStep,
  }
}

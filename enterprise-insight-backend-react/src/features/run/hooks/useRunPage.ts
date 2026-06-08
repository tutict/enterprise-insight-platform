import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useLocation } from 'react-router-dom'
import { listWorkspaces } from '../../../api/modules/workspaces.api'
import { useNotificationStore } from '../../../store/uiStore'
import { useDslStore } from '../../dsl/store/dslStore'
import { useHistoryStore } from '../../history/store/historyStore'
import { useWorkspaceStore } from '../../workspace/store/workspaceStore'
import type { StepKey } from '../model/runEvent'
import { isExecutionActive, useRunStore } from '../store/runStore'
import { DEFAULT_VERIFY_COMMAND } from '../model/runConfig'
import { useRunRuntime } from './useRunRuntime'

type RunDraftLocationState = {
  runDraft?: {
    model?: string
    workspaceId?: string
    targetDirectory?: string
    verifyCommand?: string
    maxRepairRounds?: number
  }
}

export function useRunPage() {
  const { t } = useTranslation(['run', 'dsl'])
  const location = useLocation()
  const runDraft = (location.state as RunDraftLocationState | null)?.runDraft
  const selectedWorkspaceId = useWorkspaceStore((state) => state.selectedWorkspaceId)
  const setSelectedWorkspaceId = useWorkspaceStore((state) => state.setSelectedWorkspaceId)
  const workspaces = useWorkspaceStore((state) => state.workspaces)
  const setWorkspaces = useWorkspaceStore((state) => state.setWorkspaces)
  const [workspaceId, setWorkspaceIdState] = useState(runDraft?.workspaceId ?? selectedWorkspaceId)
  const [model, setModel] = useState(runDraft?.model ?? 'llama3.1')
  const [targetDirectory, setTargetDirectory] = useState(runDraft?.targetDirectory ?? 'generated-harness-app')
  const [verifyCommand, setVerifyCommand] = useState(runDraft?.verifyCommand ?? DEFAULT_VERIFY_COMMAND)
  const [maxRepairRounds, setMaxRepairRounds] = useState(runDraft?.maxRepairRounds ?? 2)

  const dslText = useDslStore((state) => state.dslText)
  const setDslText = useDslStore((state) => state.setDslText)
  const loadDsl = useDslStore((state) => state.loadDsl)
  const execution = useRunStore((state) => state.execution)
  const runStatus = useRunStore((state) => state.status)
  const connectionState = useRunStore((state) => state.connectionState)
  const lastEventId = useRunStore((state) => state.lastEventId)

  const savedDsls = useHistoryStore((state) => state.savedDsls)
  const selectSavedDsl = useHistoryStore((state) => state.selectSavedDsl)
  const pushNotification = useNotificationStore((state) => state.push)

  const setWorkspaceId = useCallback((value: string) => {
    setWorkspaceIdState(value)
    setSelectedWorkspaceId(value)
  }, [setSelectedWorkspaceId])

  useEffect(() => {
    if (workspaces.length) {
      return
    }

    let cancelled = false
    const loadWorkspaces = async () => {
      try {
        const loaded = await listWorkspaces()
        if (!cancelled) {
          setWorkspaces(loaded)
          if (!loaded.some((workspace) => workspace.workspaceId === workspaceId) && loaded[0]) {
            setWorkspaceId(loaded[0].workspaceId)
          }
        }
      } catch (err) {
        if (!cancelled) {
          pushNotification({
            id: crypto.randomUUID(),
            type: 'error',
            message: err instanceof Error ? err.message : t('run.failed'),
          })
        }
      }
    }

    void loadWorkspaces()
    return () => {
      cancelled = true
    }
  }, [pushNotification, setWorkspaceId, setWorkspaces, t, workspaceId, workspaces.length])

  const runConfig = useMemo(
    () => ({
      workspaceId,
      model,
      targetDirectory,
      verifyCommand,
      maxRepairRounds,
    }),
    [maxRepairRounds, model, targetDirectory, verifyCommand, workspaceId],
  )
  const runtime = useRunRuntime(runConfig)

  const run = async () => {
    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: t('run.requested'),
    })

    try {
      const { execution: currentExecution, stale } = await runtime.run({
        dsl: dslText,
      })

      if (!currentExecution || stale) {
        return
      }

      const response = currentExecution.result
      if (!response) {
        pushNotification({
          id: crypto.randomUUID(),
          type: 'error',
          message: currentExecution.error ?? t('run.failed'),
        })
        return
      }

      pushNotification({
        id: crypto.randomUUID(),
        type: response.generation.successful ? 'success' : 'error',
        message: response.generation.successful
          ? t('run.completedVerified')
          : t('run.finishedWithStatus', { status: response.generation.status }),
      })
    } catch (err) {
      const message = err instanceof Error ? err.message : t('run.failed')
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message,
      })
    }
  }

  const selectDsl = (id: string) => {
    const selected = selectSavedDsl(id)
    if (selected) {
      loadDsl(selected.value)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'info',
        message: t('dsl:editor.loaded', { name: selected.name }),
      })
    }
  }

  const sendControl = async (label: string, action: () => Promise<void>) => {
    try {
      await action()
      pushNotification({
        id: crypto.randomUUID(),
        type: 'info',
        message: t('run.controlRequested', { label }),
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('run.controlFailed', { label }),
      })
    }
  }

  const retryStep = (step: StepKey) => {
    void sendControl(t('run.retryStep', { step: t(`steps.${step}`) }), () => runtime.retryStep(step))
  }

  return {
    dslText,
    setDslText,
    form: {
      workspaceId,
      model,
      targetDirectory,
      verifyCommand,
      maxRepairRounds,
    },
    setForm: {
      setWorkspaceId,
      setModel,
      setTargetDirectory,
      setVerifyCommand,
      setMaxRepairRounds,
    },
    workspaces,
    savedDsls,
    selectDsl,
    run: () => void run(),
    controls: {
      pause: () => void sendControl(t('run.pause'), runtime.pause),
      resume: () => void sendControl(t('run.resume'), runtime.resume),
      cancel: () => void sendControl(t('run.cancel'), runtime.cancel),
      retryStep,
    },
    runState: {
      phase: execution.phase,
      status: runStatus,
      connectionState,
      lastEventId,
      isRunning: isExecutionActive(execution) || runStatus === 'paused',
      error: execution.error ?? '',
      result: execution.result ?? null,
      steps: execution.steps,
      runId: execution.id,
    },
  }
}

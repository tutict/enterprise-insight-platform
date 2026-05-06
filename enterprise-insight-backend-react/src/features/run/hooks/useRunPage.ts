import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNotificationStore } from '../../../store/uiStore'
import { useDslStore } from '../../dsl/store/dslStore'
import { useHistoryStore } from '../../history/store/historyStore'
import type { StepKey } from '../model/runEvent'
import { isExecutionActive, useRunStore } from '../store/runStore'
import { DEFAULT_VERIFY_COMMAND } from '../model/runConfig'
import { useRunRuntime } from './useRunRuntime'

export function useRunPage() {
  const { t } = useTranslation(['run', 'dsl'])
  const [model, setModel] = useState('llama3.1')
  const [targetDirectory, setTargetDirectory] = useState('generated-harness-app')
  const [verifyCommand, setVerifyCommand] = useState(DEFAULT_VERIFY_COMMAND)
  const [maxRepairRounds, setMaxRepairRounds] = useState(2)

  const dslText = useDslStore((state) => state.dslText)
  const setDslText = useDslStore((state) => state.setDslText)
  const loadDsl = useDslStore((state) => state.loadDsl)
  const execution = useRunStore((state) => state.execution)
  const runStatus = useRunStore((state) => state.status)
  const connectionState = useRunStore((state) => state.connectionState)
  const lastEventId = useRunStore((state) => state.lastEventId)

  const savedDsls = useHistoryStore((state) => state.savedDsls)
  const selectSavedDsl = useHistoryStore((state) => state.selectSavedDsl)
  const addRun = useHistoryStore((state) => state.addRun)
  const pushNotification = useNotificationStore((state) => state.push)

  const runConfig = useMemo(
    () => ({
      model,
      targetDirectory,
      verifyCommand,
      maxRepairRounds,
    }),
    [maxRepairRounds, model, targetDirectory, verifyCommand],
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

      addRun({
        id: response.runId || currentExecution.id || crypto.randomUUID(),
        dsl: dslText,
        targetDirectory,
        model,
        createdAt: response.createdAt,
        response,
        phase: currentExecution.phase,
        steps: currentExecution.steps,
        eventLog: currentExecution.eventLog,
      })

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
      model,
      targetDirectory,
      verifyCommand,
      maxRepairRounds,
    },
    setForm: {
      setModel,
      setTargetDirectory,
      setVerifyCommand,
      setMaxRepairRounds,
    },
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

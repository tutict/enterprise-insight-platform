import { useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { useNotificationStore } from '../../../store/uiStore'
import type { ExecutionPhase, RunRecord, StepState, TimelineStep } from '../../run/model/runEvent'
import { useHistoryStore } from '../store/historyStore'

type LegacyStepState = Omit<StepState, 'status'> & {
  status: StepState['status'] | 'pending'
}

type PersistedRunRecord = RunRecord & {
  timeline?: LegacyStepState[]
  steps: LegacyStepState[]
  phase?: ExecutionPhase
  eventLog?: RunRecord['eventLog']
}

const normalizeStep = (step: LegacyStepState | TimelineStep): StepState => ({
  ...step,
  status: step.status === 'pending' ? 'idle' : step.status,
  attempts: step.key === 'repair' ? step.attempts ?? [] : step.attempts,
} as StepState)

const ensureRepairStep = (steps: StepState[]): StepState[] => {
  if (steps.some((step) => step.key === 'repair')) {
    return steps
  }

  return [
    ...steps,
    {
      key: 'repair',
      title: 'repair',
      status: 'idle',
      detail: 'Repair was not required for this run.',
      attempts: [],
    },
  ]
}

const normalizeRunRecord = (record: PersistedRunRecord | undefined) => {
  if (!record) {
    return null
  }
  const steps = ensureRepairStep((record.steps ?? record.timeline ?? []).map(normalizeStep))

  return {
    ...record,
    phase: record.phase ?? (record.response.generation.successful ? 'completed' : 'failed'),
    steps,
    eventLog: record.eventLog ?? [],
  }
}

export function useRunsPage() {
  const { t } = useTranslation('run')
  const storedRuns = useHistoryStore((state) => state.runs) as PersistedRunRecord[]
  const selectedRunId = useHistoryStore((state) => state.selectedRunId)
  const selectStoredRun = useHistoryStore((state) => state.selectRun)
  const pushNotification = useNotificationStore((state) => state.push)
  const runs = useMemo(
    () => storedRuns.map((run) => normalizeRunRecord(run)).filter((run): run is RunRecord => Boolean(run)),
    [storedRuns],
  )

  const selectedRun = useMemo(
    () => normalizeRunRecord(runs.find((run) => run.id === selectedRunId) ?? runs[0]),
    [runs, selectedRunId],
  )

  const selectRun = (id: string) => {
    const run = runs.find((item) => item.id === id)
    if (!run) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('history.notFound'),
      })
      return
    }

    selectStoredRun(id)
    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: `Opened run ${id}.`,
    })
  }

  return {
    runs,
    selectedRun,
    selectRun,
  }
}

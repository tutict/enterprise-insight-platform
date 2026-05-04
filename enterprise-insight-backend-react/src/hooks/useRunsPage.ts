import { useMemo } from 'react'
import { useHistoryStore } from '../store/historyStore'
import type { ExecutionPhase, RunRecord, StepState, TimelineStep } from '../store/types'

type LegacyStepState = Omit<StepState, 'status'> & {
  status: StepState['status'] | 'pending'
}

type PersistedRunRecord = RunRecord & {
  timeline?: LegacyStepState[]
  steps: LegacyStepState[]
  phase?: ExecutionPhase
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
  }
}

export function useRunsPage() {
  const storedRuns = useHistoryStore((state) => state.runs) as PersistedRunRecord[]
  const selectedRunId = useHistoryStore((state) => state.selectedRunId)
  const selectRun = useHistoryStore((state) => state.selectRun)
  const runs = useMemo(
    () => storedRuns.map((run) => normalizeRunRecord(run)).filter((run): run is RunRecord => Boolean(run)),
    [storedRuns],
  )

  const selectedRun = useMemo(
    () => normalizeRunRecord(runs.find((run) => run.id === selectedRunId) ?? runs[0]),
    [runs, selectedRunId],
  )

  return {
    runs,
    selectedRun,
    selectRun,
  }
}

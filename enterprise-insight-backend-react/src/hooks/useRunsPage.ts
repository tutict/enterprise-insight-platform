import { useMemo } from 'react'
import { useHistoryStore } from '../store/historyStore'
import type { RunRecord, TimelineStep } from '../store/types'

type PersistedRunRecord = RunRecord & {
  timeline?: TimelineStep[]
}

const normalizeRunRecord = (record: PersistedRunRecord | undefined) => {
  if (!record) {
    return null
  }

  return {
    ...record,
    steps: record.steps ?? record.timeline ?? [],
  }
}

export function useRunsPage() {
  const runs = useHistoryStore((state) => state.runs) as PersistedRunRecord[]
  const selectedRunId = useHistoryStore((state) => state.selectedRunId)
  const selectRun = useHistoryStore((state) => state.selectRun)

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

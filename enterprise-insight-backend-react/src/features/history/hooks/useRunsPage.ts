import { useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  exportWorkspaceEvidence,
  getWorkspacePatchProposal,
  getWorkspacePatchProposalDiff,
  listWorkspaceDeliveryRuns,
  listWorkspaces,
  regenerateWorkspacePatchProposal,
} from '../../../api/modules/workspaces.api'
import type { DeliveryRunRecord } from '../../../api/types/delivery.types'
import type { PatchProposal, PatchProposalDiff } from '../../../api/types/patchProposal.types'
import { useNotificationStore } from '../../../store/uiStore'
import { useWorkspaceStore } from '../../workspace/store/workspaceStore'
import { normalizeRunEvent, type BackendRunEvent } from '../../run/engine/runSSEAdapter'
import type { ExecutionPhase, RunEvent, RunRecord, StepState, TimelineStep } from '../../run/model/runEvent'
import { createInitialExecution, runReducer } from '../../run/store/runStore'

type LegacyStepState = Omit<StepState, 'status'> & {
  status: StepState['status'] | 'pending'
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

const toRunEvents = (record: DeliveryRunRecord): RunEvent[] =>
  (record.events ?? []).map((event) =>
    normalizeRunEvent(event as BackendRunEvent, event.eventId),
  )

const deriveExecution = (events: RunEvent[]) =>
  events.reduce((execution, event) => runReducer(execution, event), createInitialExecution())

const normalizeDeliveryRun = (record: DeliveryRunRecord): RunRecord => {
  const eventLog = toRunEvents(record)
  const execution = deriveExecution(eventLog)
  const request = record.request
  const response = record.response ?? execution.result ?? null
  const phase = execution.phase === 'idle'
    ? statusToPhase(record.status)
    : execution.phase

  return {
    id: record.runId,
    workspaceId: record.workspaceId,
    dsl: request?.requirement ?? response?.dsl?.requirement ?? '',
    targetDirectory: request?.targetDirectory ?? response?.generation?.projectRoot ?? '-',
    model: request?.model ?? '',
    createdAt: response?.createdAt ?? record.createdAt,
    response,
    phase,
    steps: ensureRepairStep(execution.steps.map(normalizeStep)),
    eventLog,
  }
}

const statusToPhase = (status: DeliveryRunRecord['status']): ExecutionPhase => {
  switch (status) {
    case 'COMPLETED':
      return 'completed'
    case 'FAILED':
      return 'failed'
    case 'CANCELLED':
      return 'cancelled'
    case 'RUNNING':
      return 'requested'
    default:
      return 'idle'
  }
}

export function useRunsPage() {
  const { t } = useTranslation('run')
  const [records, setRecords] = useState<DeliveryRunRecord[]>([])
  const [selectedRunId, setSelectedRunId] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [patchProposal, setPatchProposal] = useState<PatchProposal | null>(null)
  const [selectedPatchFileId, setSelectedPatchFileId] = useState<string | null>(null)
  const [patchDiffs, setPatchDiffs] = useState<Record<string, PatchProposalDiff>>({})
  const [isPatchLoading, setIsPatchLoading] = useState(false)
  const pushNotification = useNotificationStore((state) => state.push)
  const workspaces = useWorkspaceStore((state) => state.workspaces)
  const selectedWorkspaceId = useWorkspaceStore((state) => state.selectedWorkspaceId)
  const setWorkspaces = useWorkspaceStore((state) => state.setWorkspaces)
  const setSelectedWorkspaceId = useWorkspaceStore((state) => state.setSelectedWorkspaceId)

  useEffect(() => {
    let cancelled = false

    const loadRuns = async () => {
      setIsLoading(true)
      try {
        const loadedWorkspaces = workspaces.length ? workspaces : await listWorkspaces()
        setWorkspaces(loadedWorkspaces)
        const workspaceId = loadedWorkspaces.some((workspace) => workspace.workspaceId === selectedWorkspaceId)
          ? selectedWorkspaceId
          : loadedWorkspaces[0]?.workspaceId ?? selectedWorkspaceId
        const loaded = await listWorkspaceDeliveryRuns(workspaceId)
        if (cancelled) {
          return
        }
        setRecords(loaded)
        setSelectedRunId((current) => current ?? loaded[0]?.runId ?? null)
      } catch (err) {
        if (!cancelled) {
          pushNotification({
            id: crypto.randomUUID(),
            type: 'error',
            message: err instanceof Error ? err.message : t('history.loadFailed'),
          })
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadRuns()

    return () => {
      cancelled = true
    }
  }, [pushNotification, selectedWorkspaceId, setWorkspaces, t, workspaces])

  const runs = useMemo(() => records.map(normalizeDeliveryRun), [records])

  const selectedRun = useMemo(
    () => runs.find((run) => run.id === selectedRunId) ?? runs[0] ?? null,
    [runs, selectedRunId],
  )

  useEffect(() => {
    let cancelled = false

    const loadPatchProposal = async () => {
      if (!selectedRun) {
        setPatchProposal(null)
        setSelectedPatchFileId(null)
        setPatchDiffs({})
        return
      }

      setIsPatchLoading(true)
      setPatchDiffs({})
      try {
        const workspaceId = selectedRun.workspaceId ?? selectedWorkspaceId
        const proposal = await getWorkspacePatchProposal(workspaceId, selectedRun.id)
        if (cancelled) {
          return
        }
        setPatchProposal(proposal)
        const firstDiffable = proposal.files.find((file) => file.diffPath)
        setSelectedPatchFileId(firstDiffable?.fileId ?? proposal.files[0]?.fileId ?? null)
      } catch (err) {
        if (!cancelled) {
          setPatchProposal(null)
          setSelectedPatchFileId(null)
          pushNotification({
            id: crypto.randomUUID(),
            type: 'error',
            message: err instanceof Error ? err.message : t('history.loadFailed'),
          })
        }
      } finally {
        if (!cancelled) {
          setIsPatchLoading(false)
        }
      }
    }

    void loadPatchProposal()

    return () => {
      cancelled = true
    }
  }, [pushNotification, selectedRun, selectedWorkspaceId, t])

  useEffect(() => {
    let cancelled = false

    const loadDiff = async () => {
      if (!selectedRun || !patchProposal || !selectedPatchFileId || patchDiffs[selectedPatchFileId]) {
        return
      }
      const selectedFile = patchProposal.files.find((file) => file.fileId === selectedPatchFileId)
      if (!selectedFile?.diffPath) {
        return
      }

      try {
        const workspaceId = selectedRun.workspaceId ?? selectedWorkspaceId
        const diff = await getWorkspacePatchProposalDiff(workspaceId, selectedRun.id, selectedPatchFileId)
        if (!cancelled) {
          setPatchDiffs((current) => ({ ...current, [selectedPatchFileId]: diff }))
        }
      } catch (err) {
        if (!cancelled) {
          pushNotification({
            id: crypto.randomUUID(),
            type: 'error',
            message: err instanceof Error ? err.message : t('history.loadFailed'),
          })
        }
      }
    }

    void loadDiff()

    return () => {
      cancelled = true
    }
  }, [patchDiffs, patchProposal, pushNotification, selectedPatchFileId, selectedRun, selectedWorkspaceId, t])

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

    setSelectedRunId(id)
    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: t('history.opened', { id }),
    })
  }

  const exportEvidence = async (runId: string) => {
    try {
      const evidence = await exportWorkspaceEvidence(selectedWorkspaceId, runId)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: `Evidence exported: ${evidence.markdownPath}`,
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('history.loadFailed'),
      })
    }
  }

  const regeneratePatchProposal = useCallback(async () => {
    if (!selectedRun) {
      return
    }
    setIsPatchLoading(true)
    try {
      const workspaceId = selectedRun.workspaceId ?? selectedWorkspaceId
      const proposal = await regenerateWorkspacePatchProposal(workspaceId, selectedRun.id)
      setPatchProposal(proposal)
      setPatchDiffs({})
      const firstDiffable = proposal.files.find((file) => file.diffPath)
      setSelectedPatchFileId(firstDiffable?.fileId ?? proposal.files[0]?.fileId ?? null)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: 'Patch proposal regenerated.',
      })
    } catch (err) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: err instanceof Error ? err.message : t('history.loadFailed'),
      })
    } finally {
      setIsPatchLoading(false)
    }
  }, [pushNotification, selectedRun, selectedWorkspaceId, t])

  return {
    runs,
    selectedRun,
    selectRun,
    exportEvidence: (runId: string) => void exportEvidence(runId),
    patchProposal,
    selectedPatchFileId,
    selectedPatchDiff: selectedPatchFileId ? patchDiffs[selectedPatchFileId] ?? null : null,
    selectPatchFile: setSelectedPatchFileId,
    regeneratePatchProposal: () => void regeneratePatchProposal(),
    isPatchLoading,
    isLoading,
    workspaces,
    selectedWorkspaceId,
    setSelectedWorkspaceId,
  }
}

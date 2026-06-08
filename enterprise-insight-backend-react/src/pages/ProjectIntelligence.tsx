import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import {
  getWorkspaceProjectDeliveryBrief,
  getWorkspaceProjectInventory,
  listWorkspaces,
} from '../api/modules/workspaces.api'
import type { ProjectDeliveryBrief, ProjectInventory } from '../api/types/projectAnalysis.types'
import { useDslStore } from '../features/dsl/store/dslStore'
import ProjectIntelligenceWorkspace from '../features/project/components/ProjectIntelligenceWorkspace'
import { useWorkspaceStore } from '../features/workspace/store/workspaceStore'
import { useNotificationStore } from '../store/uiStore'

const firstVerifyCommand = (commands: string[][]) => commands[0]?.join(' ') || 'mvn test'

export default function ProjectIntelligence() {
  const { t } = useTranslation('common')
  const [inventory, setInventory] = useState<ProjectInventory | null>(null)
  const [brief, setBrief] = useState<ProjectDeliveryBrief | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const loadDsl = useDslStore((state) => state.loadDsl)
  const pushNotification = useNotificationStore((state) => state.push)
  const workspaces = useWorkspaceStore((state) => state.workspaces)
  const selectedWorkspaceId = useWorkspaceStore((state) => state.selectedWorkspaceId)
  const setWorkspaces = useWorkspaceStore((state) => state.setWorkspaces)
  const setSelectedWorkspaceId = useWorkspaceStore((state) => state.setSelectedWorkspaceId)

  const loadInventory = useCallback(async () => {
    setIsLoading(true)
    setError('')
    try {
      const loadedWorkspaces = workspaces.length ? workspaces : await listWorkspaces()
      setWorkspaces(loadedWorkspaces)
      const workspaceId = loadedWorkspaces.some((workspace) => workspace.workspaceId === selectedWorkspaceId)
        ? selectedWorkspaceId
        : loadedWorkspaces[0]?.workspaceId ?? selectedWorkspaceId
      const [data, deliveryBrief] = await Promise.all([
        getWorkspaceProjectInventory(workspaceId),
        getWorkspaceProjectDeliveryBrief(workspaceId),
      ])
      setInventory(data)
      setBrief(deliveryBrief)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: t('project.loaded'),
      })
    } catch (err) {
      const message = err instanceof Error ? err.message : t('project.loadFailed')
      setError(message)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message,
      })
    } finally {
      setIsLoading(false)
    }
  }, [pushNotification, selectedWorkspaceId, setWorkspaces, t, workspaces])

  const loadBriefIntoRun = useCallback(() => {
    if (!brief) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('project.brief.empty'),
      })
      return
    }

    loadDsl(brief.requirement)
    pushNotification({
      id: crypto.randomUUID(),
      type: 'success',
      message: t('project.brief.loadedIntoRun'),
    })
    navigate('/run', {
      state: {
        runDraft: {
          workspaceId: selectedWorkspaceId,
          model: 'llama3.1',
          targetDirectory: brief.targetDirectory,
          verifyCommand: firstVerifyCommand(brief.verifyCommands),
          maxRepairRounds: brief.maxRepairRounds,
        },
      },
    })
  }, [brief, loadDsl, navigate, pushNotification, selectedWorkspaceId, t])

  useEffect(() => {
    void loadInventory()
  }, [loadInventory])

  return (
    <ProjectIntelligenceWorkspace
      inventory={inventory}
      brief={brief}
      isLoading={isLoading}
      error={error}
      workspaces={workspaces}
      selectedWorkspaceId={selectedWorkspaceId}
      setSelectedWorkspaceId={setSelectedWorkspaceId}
      reload={() => void loadInventory()}
      loadBriefIntoRun={loadBriefIntoRun}
    />
  )
}

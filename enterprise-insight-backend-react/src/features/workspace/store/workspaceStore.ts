import { create } from 'zustand'
import type { Workspace } from '../../../api/types/workspace.types'

const STORAGE_KEY = 'eip.workspaceId'
export const DEFAULT_WORKSPACE_ID = 'demo-workspace'

type WorkspaceState = {
  workspaces: Workspace[]
  selectedWorkspaceId: string
  setWorkspaces: (workspaces: Workspace[]) => void
  setSelectedWorkspaceId: (workspaceId: string) => void
  selectedWorkspace: () => Workspace | null
}

const getStoredWorkspaceId = () => window.localStorage.getItem(STORAGE_KEY) || DEFAULT_WORKSPACE_ID

export const useWorkspaceStore = create<WorkspaceState>((set, get) => ({
  workspaces: [],
  selectedWorkspaceId: getStoredWorkspaceId(),
  setWorkspaces: (workspaces) =>
    set((state) => {
      const selectedExists = workspaces.some((workspace) => workspace.workspaceId === state.selectedWorkspaceId)
      const selectedWorkspaceId = selectedExists
        ? state.selectedWorkspaceId
        : workspaces[0]?.workspaceId ?? DEFAULT_WORKSPACE_ID
      window.localStorage.setItem(STORAGE_KEY, selectedWorkspaceId)
      return { workspaces, selectedWorkspaceId }
    }),
  setSelectedWorkspaceId: (selectedWorkspaceId) => {
    window.localStorage.setItem(STORAGE_KEY, selectedWorkspaceId)
    set({ selectedWorkspaceId })
  },
  selectedWorkspace: () =>
    get().workspaces.find((workspace) => workspace.workspaceId === get().selectedWorkspaceId) ?? null,
}))

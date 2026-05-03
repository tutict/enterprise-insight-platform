import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { RunRecord, SavedDsl } from './types'
import { useNotificationStore } from './notifications'

type HistoryState = {
  savedDsls: SavedDsl[]
  runs: RunRecord[]
  selectedRunId: string | null
  saveDsl: (name: string, value: string) => void
  selectSavedDsl: (id: string) => SavedDsl | null
  addRun: (record: RunRecord) => void
  selectRun: (id: string) => void
}

const notify = (type: 'error' | 'info' | 'success', message: string) => {
  useNotificationStore.getState().push({
    id: crypto.randomUUID(),
    type,
    message,
  })
}

export const useHistoryStore = create<HistoryState>()(
  persist(
    (set, get) => ({
      savedDsls: [],
      runs: [],
      selectedRunId: null,
      saveDsl: (name, value) => {
        if (!value.trim()) {
          notify('error', 'DSL is empty. Nothing was saved.')
          return
        }
        const next: SavedDsl = {
          id: crypto.randomUUID(),
          name: name.trim() || `DSL ${new Date().toLocaleString()}`,
          value,
          updatedAt: new Date().toISOString(),
        }
        set((state) => ({ savedDsls: [next, ...state.savedDsls] }))
        notify('success', `Saved ${next.name}.`)
      },
      selectSavedDsl: (id) => {
        const item = get().savedDsls.find((dsl) => dsl.id === id) ?? null
        if (item) {
          notify('info', `Loaded ${item.name}.`)
        }
        return item
      },
      addRun: (record) => {
        set((state) => ({
          runs: [record, ...state.runs].slice(0, 50),
          selectedRunId: record.id,
        }))
      },
      selectRun: (id) => {
        const run = get().runs.find((item) => item.id === id)
        if (!run) {
          notify('error', 'Run record was not found.')
          return
        }
        set({ selectedRunId: id })
        notify('info', `Opened run ${run.id}.`)
      },
    }),
    {
      name: 'orchestrator-history',
    },
  ),
)

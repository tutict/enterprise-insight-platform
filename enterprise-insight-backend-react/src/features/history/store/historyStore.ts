import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { RunRecord } from '../../run/model/runEvent'
import type { SavedDsl } from './historyTypes'

type HistoryState = {
  savedDsls: SavedDsl[]
  runs: RunRecord[]
  selectedRunId: string | null
  saveDsl: (name: string, value: string) => void
  selectSavedDsl: (id: string) => SavedDsl | null
  addRun: (record: RunRecord) => void
  selectRun: (id: string) => void
}

export const useHistoryStore = create<HistoryState>()(
  persist(
    (set, get) => ({
      savedDsls: [],
      runs: [],
      selectedRunId: null,
      saveDsl: (name, value) => {
        if (!value.trim()) {
          return
        }
        const next: SavedDsl = {
          id: crypto.randomUUID(),
          name: name.trim() || `DSL ${new Date().toLocaleString()}`,
          value,
          updatedAt: new Date().toISOString(),
        }
        set((state) => ({ savedDsls: [next, ...state.savedDsls] }))
      },
      selectSavedDsl: (id) => {
        return get().savedDsls.find((dsl) => dsl.id === id) ?? null
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
          return
        }
        set({ selectedRunId: id })
      },
    }),
    {
      name: 'orchestrator-history',
    },
  ),
)

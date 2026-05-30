import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { SavedDsl } from './historyTypes'

type HistoryState = {
  savedDsls: SavedDsl[]
  saveDsl: (name: string, value: string) => void
  selectSavedDsl: (id: string) => SavedDsl | null
}

export const useHistoryStore = create<HistoryState>()(
  persist(
    (set, get) => ({
      savedDsls: [],
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
    }),
    {
      name: 'orchestrator-history',
      partialize: (state) => ({ savedDsls: state.savedDsls }),
    },
  ),
)

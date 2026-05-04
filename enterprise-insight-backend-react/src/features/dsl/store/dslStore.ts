import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { CompileResponse } from '../../../api/types/compiler.types'

export type AsyncStatus = 'idle' | 'loading' | 'success' | 'error'

type DslState = {
  dslText: string
  prompt: string
  compileResult: CompileResponse | null
  compileStatus: AsyncStatus
  compileError: string
  setDslText: (value: string) => void
  loadDsl: (value: string) => void
  compileStarted: () => void
  compileSucceeded: (result: CompileResponse) => void
  compileFailed: (error: string) => void
}

const defaultDsl = [
  'project:',
  '  type: spring_boot',
  '  modules:',
  '    - user',
  '    - auth',
  'constraints:',
  '  db: mysql',
  '  language: Java 21',
].join('\n')

export const useDslStore = create<DslState>()(
  persist(
    (set) => ({
      dslText: defaultDsl,
      prompt: '',
      compileResult: null,
      compileStatus: 'idle',
      compileError: '',
      setDslText: (value) => set({ dslText: value }),
      loadDsl: (value) => set({ dslText: value, compileError: '' }),
      compileStarted: () => set({ compileStatus: 'loading', compileError: '', prompt: '' }),
      compileSucceeded: (result) =>
        set({
          compileResult: result,
          prompt: result.prompt,
          compileStatus: 'success',
        }),
      compileFailed: (error) => set({ compileError: error, compileStatus: 'error' }),
    }),
    {
      name: 'dsl-workspace',
      partialize: (state) => ({
        dslText: state.dslText,
        prompt: state.prompt,
      }),
    },
  ),
)

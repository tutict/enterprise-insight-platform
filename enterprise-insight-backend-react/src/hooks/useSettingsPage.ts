import { getApiBaseUrl } from '../api/client'

export function useSettingsPage() {
  return {
    runtimeValues: [
      {
        label: 'API base URL',
        value: getApiBaseUrl() || 'same-origin / Vite proxy',
      },
      {
        label: 'Compiler endpoint',
        value: '/api/compiler/compile',
      },
      {
        label: 'Orchestrator endpoint',
        value: '/api/orchestrator/run',
      },
    ],
    executionContract: [
      'DSL Editor sends YAML text through the compiler request body.',
      'Run sends the same DSL text to the orchestrator as the requirement field.',
      'Timeline status is derived from actual orchestrator lifecycle and response fields.',
    ],
  }
}

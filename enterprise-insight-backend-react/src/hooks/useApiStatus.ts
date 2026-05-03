import { useEffect, useState } from 'react'
import { apiHealthCheck } from '../api/client'

type ApiState = 'checking' | 'ok' | 'down'

type ApiStatus = {
  state: ApiState
  latencyMs?: number
  message?: string
}

export default function useApiStatus() {
  const [status, setStatus] = useState<ApiStatus>({ state: 'checking' })

  useEffect(() => {
    const controller = new AbortController()
    const startedAt = Date.now()
    const timeoutId = window.setTimeout(() => controller.abort(), 4000)

    const check = async () => {
      try {
        const isHealthy = await apiHealthCheck()
        const latencyMs = Date.now() - startedAt
        if (!isHealthy) {
          setStatus({
            state: 'down',
            latencyMs,
            message: 'Unreachable',
          })
          return
        }
        setStatus({ state: 'ok', latencyMs, message: 'Online' })
      } catch (error) {
        if ((error as Error).name === 'AbortError') {
          setStatus({ state: 'down', message: 'Timeout' })
          return
        }
        setStatus({ state: 'down', message: 'Unreachable' })
      }
    }

    check()

    return () => {
      controller.abort()
      window.clearTimeout(timeoutId)
    }
  }, [])

  return status
}

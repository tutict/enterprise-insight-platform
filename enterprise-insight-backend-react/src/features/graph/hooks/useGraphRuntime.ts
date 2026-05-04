import { useCallback } from 'react'
import { compileGraph, startGraphRun } from '../../../api/modules/graph.api'
import type { GraphDefinition, GraphRunRequest } from '../../../api/types/graph.types'
import { graphStream } from '../engine/graphStream'
import { useGraphStore } from '../store/graphStore'

export function useGraphRuntime() {
  const dispatch = useGraphStore((state) => state.dispatch)
  const reset = useGraphStore((state) => state.reset)

  const run = useCallback(
    async (request: GraphRunRequest = {}) => {
      reset()
      const started = await startGraphRun(request)

      for await (const event of graphStream(started.runId)) {
        dispatch(event)
      }
    },
    [dispatch, reset],
  )

  const compile = useCallback((graph: GraphDefinition) => compileGraph(graph), [])

  return {
    compile,
    run,
  }
}

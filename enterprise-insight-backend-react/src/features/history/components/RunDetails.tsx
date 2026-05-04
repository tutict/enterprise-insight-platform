import CodeBlock from '../../../components/CodeBlock'
import CodeOutput from '../../../components/CodeOutput'
import ExecutionTimeline from '../../../components/ExecutionTimeline'
import type { RunRecord } from '../../../store/types'

type RunDetailsProps = {
  run: RunRecord | null
}

export default function RunDetails({ run }: RunDetailsProps) {
  return (
    <section className="space-y-5">
      {run ? (
        <>
          <ExecutionTimeline steps={run.steps} />
          <div className="panel p-5">
            <h3 className="mb-3 text-sm font-semibold text-slate-100">Generated Output</h3>
            <CodeOutput value={run.response.generation.finalOutput} />
          </div>
          <div className="panel p-5">
            <h3 className="mb-3 text-sm font-semibold text-slate-100">DSL</h3>
            <CodeBlock value={run.dsl} collapsible />
          </div>
        </>
      ) : (
        <div className="panel p-8 text-sm text-slate-500">Select a run to inspect details.</div>
      )}
    </section>
  )
}

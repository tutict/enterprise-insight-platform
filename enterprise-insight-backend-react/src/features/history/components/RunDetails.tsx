import { useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import CodeBlock from '../../../shared/components/CodeBlock'
import CodeOutput from '../../run/components/CodeOutput'
import ExecutionTimeline from '../../run/components/ExecutionTimeline'
import FlowCanvas from '../../run/components/flow/FlowCanvas'
import { createWorkflowGraph } from '../../run/hooks/useFlowData'
import type { RunRecord } from '../../run/model/runEvent'
import type { PatchProposal, PatchProposalDiff } from '../../../api/types/patchProposal.types'
import PatchProposalPanel from './PatchProposalPanel'

type RunDetailsProps = {
  run: RunRecord | null
  exportEvidence: (id: string) => void
  patchProposal: PatchProposal | null
  selectedPatchFileId: string | null
  selectedPatchDiff: PatchProposalDiff | null
  selectPatchFile: (fileId: string) => void
  regeneratePatchProposal: () => void
  isPatchLoading: boolean
}

export default function RunDetails({
  run,
  exportEvidence,
  patchProposal,
  selectedPatchFileId,
  selectedPatchDiff,
  selectPatchFile,
  regeneratePatchProposal,
  isPatchLoading,
}: RunDetailsProps) {
  const { t } = useTranslation(['run', 'dsl'])
  const workflow = useMemo(() => {
    if (!run) {
      return { nodes: [], edges: [] }
    }

    return createWorkflowGraph(
      run.steps,
      run.phase,
    )
  }, [run])

  return (
    <section className="space-y-5">
      {run ? (
        <>
          <div className="panel p-5">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
              <div>
              <h2 className="text-sm font-semibold text-slate-100">{t('run.workflowGraph')}</h2>
              <p className="muted">{t('run.workflowPath')}</p>
              </div>
              <button className="btn-secondary" type="button" onClick={() => exportEvidence(run.id)}>
                Export Evidence
              </button>
            </div>
            <FlowCanvas nodes={workflow.nodes} edges={workflow.edges} />
          </div>
          <ExecutionTimeline steps={run.steps} />
          <PatchProposalPanel
            proposal={patchProposal}
            selectedFileId={selectedPatchFileId}
            selectedDiff={selectedPatchDiff}
            isLoading={isPatchLoading}
            onSelectFile={selectPatchFile}
            onRegenerate={regeneratePatchProposal}
          />
          {run.response ? (
            <div className="panel p-5">
              <h3 className="mb-3 text-sm font-semibold text-slate-100">{t('history.generatedOutput')}</h3>
              <CodeOutput value={run.response.generation.finalOutput} />
            </div>
          ) : null}
          <div className="panel p-5">
            <h3 className="mb-3 text-sm font-semibold text-slate-100">{t('dsl:editor.title')}</h3>
            <CodeBlock value={run.dsl} collapsible />
          </div>
        </>
      ) : (
        <div className="panel grid min-h-72 place-items-center p-8 text-center text-sm text-slate-500">
          {t('history.selectRun')}
        </div>
      )}
    </section>
  )
}

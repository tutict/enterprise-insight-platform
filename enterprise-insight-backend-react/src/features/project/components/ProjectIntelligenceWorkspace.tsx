import { useTranslation } from 'react-i18next'
import type {
  ApiEndpointEvidence,
  BusinessCapability,
  CodeEvidence,
  DeliveryOpportunity,
  FrontendRouteEvidence,
  ProjectDeliveryBrief,
  ProjectInventory,
  ProjectModule,
} from '../../../api/types/projectAnalysis.types'

type ProjectIntelligenceWorkspaceProps = {
  inventory: ProjectInventory | null
  brief: ProjectDeliveryBrief | null
  isLoading: boolean
  error: string
  reload: () => void
  loadBriefIntoRun: () => void
}

const metricValue = (value: number) => value.toLocaleString()
const formatVerifyCommands = (commands: string[][]) => commands.map((command) => command.join(' ')).join(', ')

export default function ProjectIntelligenceWorkspace({
  inventory,
  brief,
  isLoading,
  error,
  reload,
  loadBriefIntoRun,
}: ProjectIntelligenceWorkspaceProps) {
  const { t } = useTranslation('common')
  const summary = inventory?.summary
  const modules = inventory?.modules ?? []
  const apiEndpoints = inventory?.apiEndpoints ?? []
  const frontendRoutes = inventory?.frontendRoutes ?? []
  const businessCapabilities = inventory?.businessCapabilities ?? []
  const documents = inventory?.documents ?? []
  const tests = inventory?.tests ?? []
  const deliveryOpportunities = inventory?.deliveryOpportunities ?? []

  return (
    <div className="space-y-5">
      <section className="panel p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="min-w-0">
            <h2 className="text-lg font-semibold text-slate-100">{t('project.title')}</h2>
            <p className="muted mt-1">{t('project.description')}</p>
          </div>
          <button className="btn-secondary" type="button" onClick={reload} disabled={isLoading}>
            {isLoading ? t('project.scanning') : t('project.rescan')}
          </button>
        </div>
        {summary ? (
          <div className="mt-4 grid gap-3 text-sm text-slate-300 sm:grid-cols-2 xl:grid-cols-4">
            <Metric label={t('project.metrics.files')} value={metricValue(summary.scannedFiles)} />
            <Metric label={t('project.metrics.modules')} value={metricValue(summary.moduleCount)} />
            <Metric label={t('project.metrics.apis')} value={metricValue(summary.apiEndpointCount)} />
            <Metric label={t('project.metrics.capabilities')} value={metricValue(summary.businessCapabilityCount)} />
          </div>
        ) : null}
      </section>

      {error ? (
        <section className="rounded-lg border border-red-400/30 bg-red-950/60 p-4 text-sm text-red-100">
          <p className="font-medium">{t('project.loadFailed')}</p>
          <p className="mt-1 text-red-100/80">{error}</p>
        </section>
      ) : null}

      {!inventory && !error ? (
        <section className="panel grid min-h-72 place-items-center p-8 text-center text-sm text-slate-500">
          {isLoading ? t('project.scanning') : t('project.empty')}
        </section>
      ) : null}

      {summary ? (
        <>
          {brief ? <BriefPanel brief={brief} loadBriefIntoRun={loadBriefIntoRun} /> : null}

          <section className="panel p-5">
            <div className="mb-4">
              <h3 className="text-sm font-semibold text-slate-100">{t('project.opportunities.title')}</h3>
              <p className="muted">{t('project.opportunities.description')}</p>
            </div>
            <div className="divide-y divide-slate-700/80">
              {deliveryOpportunities.map((opportunity) => (
                <OpportunityRow key={opportunity.title} opportunity={opportunity} />
              ))}
            </div>
          </section>

          <div className="grid gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(380px,1fr)]">
            <section className="panel overflow-hidden">
              <SectionHeader title={t('project.modules.title')} description={t('project.modules.description')} />
              <div className="divide-y divide-slate-700/80">
                {modules.map((module) => (
                  <ModuleRow key={module.path} module={module} />
                ))}
              </div>
            </section>

            <section className="panel overflow-hidden">
              <SectionHeader title={t('project.capabilities.title')} description={t('project.capabilities.description')} />
              <div className="divide-y divide-slate-700/80">
                {businessCapabilities.slice(0, 12).map((capability) => (
                  <CapabilityRow key={capability.name} capability={capability} />
                ))}
              </div>
            </section>
          </div>

          <div className="grid gap-5 xl:grid-cols-2">
            <EvidencePanel
              title={t('project.api.title')}
              description={t('project.api.description')}
              items={apiEndpoints.slice(0, 20)}
              renderItem={(item) => <ApiEndpointRow endpoint={item} />}
            />
            <EvidencePanel
              title={t('project.routes.title')}
              description={t('project.routes.description')}
              items={frontendRoutes.slice(0, 20)}
              renderItem={(item) => <FrontendRouteRow route={item} />}
            />
          </div>

          <div className="grid gap-5 xl:grid-cols-2">
            <CodeEvidencePanel
              title={t('project.documents.title')}
              description={t('project.documents.description')}
              evidence={documents.slice(0, 20)}
            />
            <CodeEvidencePanel
              title={t('project.tests.title')}
              description={t('project.tests.description')}
              evidence={tests.slice(0, 20)}
            />
          </div>
        </>
      ) : null}
    </div>
  )
}

function BriefPanel({
  brief,
  loadBriefIntoRun,
}: {
  brief: ProjectDeliveryBrief
  loadBriefIntoRun: () => void
}) {
  const { t } = useTranslation('common')

  return (
    <section className="panel overflow-hidden">
      <div className="grid gap-4 border-b border-slate-700/80 p-5 xl:grid-cols-[minmax(0,1fr)_auto]">
        <div className="min-w-0">
          <h3 className="text-sm font-semibold text-slate-100">{t('project.brief.title')}</h3>
          <p className="muted mt-1">{t('project.brief.description')}</p>
        </div>
        <button className="btn-primary" type="button" onClick={loadBriefIntoRun}>
          {t('project.brief.loadRun')}
        </button>
      </div>
      <div className="grid gap-4 p-5 xl:grid-cols-[minmax(0,1fr)_minmax(300px,0.45fr)]">
        <pre className="max-h-72 overflow-auto whitespace-pre-wrap rounded-md border border-slate-700/80 bg-console-950 p-4 text-xs leading-5 text-slate-300">{brief.requirement}</pre>
        <dl className="grid content-start gap-3 text-sm text-slate-300">
          <div className="rounded-md border border-slate-700/80 bg-console-950 px-3 py-2">
            <dt className="text-xs text-slate-500">{t('project.brief.playbook')}</dt>
            <dd className="mt-1 truncate">{brief.playbookName}</dd>
          </div>
          <div className="rounded-md border border-slate-700/80 bg-console-950 px-3 py-2">
            <dt className="text-xs text-slate-500">{t('project.brief.targetDirectory')}</dt>
            <dd className="mt-1 truncate">{brief.targetDirectory}</dd>
          </div>
          <div className="rounded-md border border-slate-700/80 bg-console-950 px-3 py-2">
            <dt className="text-xs text-slate-500">{t('project.brief.verifyCommands')}</dt>
            <dd className="mt-1 truncate">{formatVerifyCommands(brief.verifyCommands)}</dd>
          </div>
          <div className="rounded-md border border-slate-700/80 bg-console-950 px-3 py-2">
            <dt className="text-xs text-slate-500">{t('project.brief.evidence')}</dt>
            <dd className="mt-1">{brief.evidence.length.toLocaleString()}</dd>
          </div>
        </dl>
      </div>
    </section>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-slate-700/80 bg-console-950 px-3 py-2">
      <p className="text-xs text-slate-500">{label}</p>
      <p className="mt-1 text-lg font-semibold text-slate-100">{value}</p>
    </div>
  )
}

function SectionHeader({ title, description }: { title: string; description: string }) {
  return (
    <div className="border-b border-slate-700/80 p-5">
      <h3 className="text-sm font-semibold text-slate-100">{title}</h3>
      <p className="muted mt-1">{description}</p>
    </div>
  )
}

function ModuleRow({ module }: { module: ProjectModule }) {
  return (
    <div className="grid gap-2 px-5 py-4 text-sm sm:grid-cols-[minmax(0,1fr)_120px_90px]">
      <div className="min-w-0">
        <p className="truncate font-medium text-slate-100">{module.path}</p>
        <p className="mt-1 truncate text-xs text-slate-500">{module.markers.join(', ') || '-'}</p>
      </div>
      <span className="text-slate-300">{module.type}</span>
      <span className="text-slate-500">{module.fileCount.toLocaleString()}</span>
    </div>
  )
}

function CapabilityRow({ capability }: { capability: BusinessCapability }) {
  const firstEvidence = capability.evidence[0]
  return (
    <div className="grid gap-2 px-5 py-4 text-sm sm:grid-cols-[minmax(0,1fr)_80px]">
      <div className="min-w-0">
        <p className="truncate font-medium text-slate-100">{capability.name}</p>
        <p className="mt-1 truncate text-xs text-slate-500">
          {firstEvidence ? `${firstEvidence.kind}: ${firstEvidence.sourcePath}:${firstEvidence.line}` : '-'}
        </p>
      </div>
      <span className="text-slate-400">{capability.evidenceCount.toLocaleString()}</span>
    </div>
  )
}

function OpportunityRow({ opportunity }: { opportunity: DeliveryOpportunity }) {
  const firstEvidence = opportunity.evidence[0]
  return (
    <div className="grid gap-3 py-4 text-sm xl:grid-cols-[56px_minmax(0,1fr)]">
      <span className="inline-flex h-8 w-12 items-center justify-center rounded-md border border-teal-300/40 bg-teal-300/10 text-xs font-semibold text-teal-100">
        {opportunity.priority}
      </span>
      <div className="min-w-0">
        <p className="font-medium text-slate-100">{opportunity.title}</p>
        <p className="mt-1 text-slate-400">{opportunity.rationale}</p>
        {firstEvidence ? (
          <p className="mt-2 truncate text-xs text-slate-500">
            {firstEvidence.kind}: {firstEvidence.sourcePath}:{firstEvidence.line}
          </p>
        ) : null}
      </div>
    </div>
  )
}

function EvidencePanel<T>({
  title,
  description,
  items,
  renderItem,
}: {
  title: string
  description: string
  items: T[]
  renderItem: (item: T) => JSX.Element
}) {
  return (
    <section className="panel overflow-hidden">
      <SectionHeader title={title} description={description} />
      <div className="divide-y divide-slate-700/80">
        {items.map((item, index) => (
          <div key={index}>{renderItem(item)}</div>
        ))}
      </div>
    </section>
  )
}

function ApiEndpointRow({ endpoint }: { endpoint: ApiEndpointEvidence }) {
  return (
    <div className="grid gap-2 px-5 py-4 text-sm sm:grid-cols-[70px_minmax(0,1fr)]">
      <span className="font-semibold text-teal-100">{endpoint.method}</span>
      <div className="min-w-0">
        <p className="truncate text-slate-100">{endpoint.path}</p>
        <p className="mt-1 truncate text-xs text-slate-500">
          {endpoint.sourcePath}:{endpoint.line}
        </p>
      </div>
    </div>
  )
}

function FrontendRouteRow({ route }: { route: FrontendRouteEvidence }) {
  return (
    <div className="px-5 py-4 text-sm">
      <p className="truncate text-slate-100">{route.path}</p>
      <p className="mt-1 truncate text-xs text-slate-500">
        {route.sourcePath}:{route.line}
      </p>
    </div>
  )
}

function CodeEvidencePanel({
  title,
  description,
  evidence,
}: {
  title: string
  description: string
  evidence: CodeEvidence[]
}) {
  return (
    <section className="panel overflow-hidden">
      <SectionHeader title={title} description={description} />
      <div className="divide-y divide-slate-700/80">
        {evidence.map((item) => (
          <div key={`${item.sourcePath}:${item.line}`} className="px-5 py-4 text-sm">
            <p className="truncate text-slate-100">{item.name}</p>
            <p className="mt-1 truncate text-xs text-slate-500">
              {item.sourcePath}:{item.line}
            </p>
          </div>
        ))}
      </div>
    </section>
  )
}

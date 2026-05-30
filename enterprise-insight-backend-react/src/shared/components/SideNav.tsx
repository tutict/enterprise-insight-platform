import { NavLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

const navItems = [
  { labelKey: 'nav.dsl', path: '/dsl' },
  { labelKey: 'nav.graph', path: '/graph' },
  { labelKey: 'nav.run', path: '/run' },
  { labelKey: 'nav.runs', path: '/runs' },
  { labelKey: 'nav.settings', path: '/settings' },
]

const pipelineSteps = ['nav.steps.compile', 'nav.steps.generate', 'nav.steps.verify']

function SideNav() {
  const { t } = useTranslation('common')

  return (
    <>
      <aside className="hidden w-72 shrink-0 border-r border-slate-800 bg-console-900/95 px-4 py-5 lg:block">
        <div className="mb-8 flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-300 text-sm font-black text-slate-950">
            FDE
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-slate-100">{t('app.brand')}</p>
            <p className="truncate text-xs text-slate-500">{t('app.console')}</p>
          </div>
        </div>

        <nav className="space-y-1" aria-label={t('app.brand')}>
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `group flex min-h-11 items-center gap-3 rounded-md px-3 text-sm transition ${
                  isActive
                    ? 'bg-teal-300/12 text-teal-100'
                    : 'text-slate-400 hover:bg-slate-800/80 hover:text-slate-100'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <span
                    className={`h-5 w-1 rounded-full ${
                      isActive ? 'bg-teal-300' : 'bg-slate-700 group-hover:bg-slate-500'
                    }`}
                    aria-hidden="true"
                  />
                  <span className="truncate">{t(item.labelKey)}</span>
                </>
              )}
            </NavLink>
          ))}
        </nav>

        <div className="mt-8 rounded-lg border border-slate-700/80 bg-console-950/70 p-4">
          <p className="text-xs font-medium uppercase text-slate-500">{t('nav.pipeline')}</p>
          <div className="mt-3 space-y-2 text-sm text-slate-300">
            {pipelineSteps.map((step, index) => (
              <div key={step} className="flex items-center gap-3">
                <span className="grid h-6 w-6 shrink-0 place-items-center rounded-md border border-slate-700 text-xs text-slate-400">
                  {index + 1}
                </span>
                <span>{t(step)}</span>
              </div>
            ))}
          </div>
        </div>
      </aside>

      <div className="border-b border-slate-800 bg-console-900/95 lg:hidden">
        <div className="flex items-center gap-3 px-4 py-3">
          <div className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-teal-300 text-xs font-black text-slate-950">
            FDE
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-slate-100">{t('app.brand')}</p>
            <p className="truncate text-xs text-slate-500">{t('app.console')}</p>
          </div>
        </div>
        <nav className="flex gap-2 overflow-x-auto px-3 pb-3" aria-label={t('app.brand')}>
          {navItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `inline-flex min-h-11 shrink-0 items-center rounded-md border px-3 text-sm transition ${
                  isActive
                    ? 'border-teal-300/50 bg-teal-300/12 text-teal-100'
                    : 'border-slate-700 bg-slate-900/70 text-slate-400 hover:border-slate-500 hover:text-slate-100'
                }`
              }
            >
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>
      </div>
    </>
  )
}

export default SideNav

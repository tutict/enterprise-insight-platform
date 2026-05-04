import { NavLink } from 'react-router-dom'

const navItems = [
  { label: 'DSL Editor', path: '/dsl' },
  { label: 'Graph', path: '/graph' },
  { label: 'Run', path: '/run' },
  { label: 'Runs', path: '/runs' },
  { label: 'Settings', path: '/settings' },
]

function SideNav() {
  return (
    <aside className="hidden w-72 shrink-0 border-r border-white/10 bg-console-900/90 px-4 py-5 lg:block">
      <div className="mb-8 flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-400 text-sm font-black text-slate-950">
          AO
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-100">AI Orchestrator</p>
          <p className="text-xs text-slate-500">Control Console</p>
        </div>
      </div>

      <nav className="space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-md px-3 py-2 text-sm transition ${
                isActive
                  ? 'bg-teal-400/12 text-teal-200'
                  : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'
              }`
            }
          >
            <span className="h-1.5 w-1.5 rounded-full bg-current" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-8 rounded-lg border border-white/10 bg-white/[0.03] p-4">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-500">Pipeline</p>
        <div className="mt-3 space-y-2 text-sm text-slate-300">
          <p>compile</p>
          <p>generate</p>
          <p>verify</p>
        </div>
      </div>
    </aside>
  )
}

export default SideNav

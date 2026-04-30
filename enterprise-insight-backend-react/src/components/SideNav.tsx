import { NavLink } from 'react-router-dom'

const navItems = [
  { label: 'Harness', path: '/harness' },
  { label: 'Runs', path: '/runs' },
  { label: 'Templates', path: '/templates' },
  { label: 'Agents', path: '/agents' },
  { label: 'Settings', path: '/settings' },
]

function SideNav() {
  return (
    <aside className="side-nav">
      <div className="brand">
        <div className="brand-mark">
          <span />
          <span />
          <span />
        </div>
        <div>
          <p className="brand-title">Harness Compiler</p>
          <p className="brand-subtitle">AI Engineering Console</p>
        </div>
      </div>

      <nav className="nav-links">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `nav-link${isActive ? ' active' : ''}`
            }
          >
            <span className="nav-dot" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="side-card">
        <p className="side-card-title">Agent Loop</p>
        <p className="side-card-value">Compiler ready</p>
        <div className="side-card-row">
          <span className="status-dot status-dot--ok" />
          Prompt compiler online
        </div>
        <div className="side-card-row">
          <span className="status-dot status-dot--warn" />
          Ollama local model
        </div>
      </div>
    </aside>
  )
}

export default SideNav

import { useAuth } from '../auth/AuthContext'

function TopBar() {
  const { auth, logout } = useAuth()
  return (
    <header className="top-bar">
      <div>
        <p className="eyebrow">Engineering AI</p>
        <h1 className="page-title">AI Harness Compiler</h1>
      </div>
      <div className="top-actions">
        <div className="search-shell">
          <span className="search-icon" aria-hidden>
            S
          </span>
          <input
            type="search"
            placeholder="Search runs, prompts, agents..."
            aria-label="Search"
          />
        </div>
        <button className="ghost-btn" type="button">
          New DSL
        </button>
        <button className="primary-btn" type="button">
          Run Agent
        </button>
        {auth ? (
          <div className="user-chip">
            <div>
              <p className="user-name">{auth.username}</p>
              <p className="user-meta">
                {auth.roles.join(', ')} - {auth.tenant ?? 'demo-tenant'}
              </p>
            </div>
            <button
              className="ghost-btn ghost-btn--small"
              type="button"
              onClick={logout}
            >
              Sign out
            </button>
          </div>
        ) : null}
      </div>
    </header>
  )
}

export default TopBar

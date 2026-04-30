import useApiStatus from '../hooks/useApiStatus'

const runs = [
  {
    id: 'run-compiler-001',
    target: 'generated-harness-app',
    status: 'Verified',
    attempts: 2,
  },
  {
    id: 'run-compiler-002',
    target: 'auth-service-skeleton',
    status: 'Prompt compiled',
    attempts: 1,
  },
  {
    id: 'run-compiler-003',
    target: 'leaderboard-module',
    status: 'Waiting for agent',
    attempts: 0,
  },
]

function Runs() {
  const apiStatus = useApiStatus()
  const statusLabel =
    apiStatus.state === 'checking'
      ? 'Checking'
      : apiStatus.state === 'ok'
        ? 'Online'
        : 'Offline'

  return (
    <div className="runs-page">
      <section className="hero glass-card">
        <div>
          <p className="eyebrow">Run control</p>
          <h2>Track compiler and agent execution.</h2>
          <p className="hero-subtitle">
            Monitor DSL compilation, Prompt generation, local model execution,
            and automatic repair attempts.
          </p>
        </div>
        <div className="hero-panel">
          <div className="status-card">
            <p className="status-title">API status</p>
            <div className={`status-pill status-pill--${apiStatus.state}`}>
              <span />
              {statusLabel}
            </div>
            <p className="status-meta">
              {apiStatus.message ?? 'Health check ready'}
              {apiStatus.latencyMs ? ` - ${apiStatus.latencyMs}ms` : ''}
            </p>
          </div>
          <div className="status-card">
            <p className="status-title">Repair policy</p>
            <p className="status-value">2 rounds</p>
            <p className="status-meta">Stops on verified build or max attempts</p>
          </div>
        </div>
      </section>

      <section className="glass-card">
        <div className="card-header">
          <h3>Recent harness runs</h3>
          <span className="chip">local</span>
        </div>
        <div className="table">
          <div className="table-row table-head">
            <span>Run</span>
            <span>Target</span>
            <span>Status</span>
            <span>Attempts</span>
          </div>
          {runs.map((run) => (
            <div key={run.id} className="table-row">
              <span>{run.id}</span>
              <span>{run.target}</span>
              <span>{run.status}</span>
              <span>{run.attempts}</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default Runs

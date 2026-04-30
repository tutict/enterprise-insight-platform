const agents = [
  {
    provider: 'Ollama',
    model: 'llama3.1',
    endpoint: 'http://localhost:11434/api/generate',
    status: 'Local',
  },
  {
    provider: 'Agent Adapter',
    model: 'file-block-writer',
    endpoint: '/api/agent-adapter/auto-repair/generate',
    status: 'Ready',
  },
  {
    provider: 'Verifier',
    model: 'ProcessBuilder',
    endpoint: 'mvn test',
    status: 'Sandboxed command',
  },
]

function Systems() {
  return (
    <div className="agents-page">
      <section className="glass-card">
        <div className="card-header">
          <h2>Agent runtime</h2>
          <button className="ghost-btn" type="button">
            Refresh
          </button>
        </div>
        <p className="muted">
          Local model execution, project file writing, and verification runners
          are managed as explicit adapters.
        </p>
        <div className="table">
          <div className="table-row table-head">
            <span>Provider</span>
            <span>Model</span>
            <span>Endpoint</span>
            <span>Status</span>
          </div>
          {agents.map((agent) => (
            <div key={`${agent.provider}-${agent.model}`} className="table-row">
              <span>{agent.provider}</span>
              <span>{agent.model}</span>
              <span className="truncate">{agent.endpoint}</span>
              <span>{agent.status}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="glass-card system-note">
        <h3>Execution safety</h3>
        <p>
          Generated file paths are restricted to the configured output root.
          Verification stops on timeout, command failure, or successful build.
        </p>
      </section>
    </div>
  )
}

export default Systems

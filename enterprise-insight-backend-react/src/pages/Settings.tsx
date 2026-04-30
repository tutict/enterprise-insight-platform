function Settings() {
  return (
    <div className="settings">
      <section className="glass-card">
        <div className="card-header">
          <h2>Harness settings</h2>
          <button className="ghost-btn" type="button">
            Save changes
          </button>
        </div>
        <div className="form-grid">
          <label>
            Workspace name
            <input type="text" defaultValue="AI Harness Lab" />
          </label>
          <label>
            Default model
            <input type="text" defaultValue="llama3.1" />
          </label>
          <label>
            Max repair rounds
            <input type="number" defaultValue={2} />
          </label>
          <label>
            Verification command
            <input type="text" defaultValue="mvn test" />
          </label>
        </div>
      </section>

      <section className="glass-card">
        <h3>Generation contract</h3>
        <p className="muted">
          Agents must return complete files using the file-block protocol so the
          platform can write, verify, and repair projects deterministically.
        </p>
        <div className="pill-row">
          <span className="chip">DSL</span>
          <span className="chip">Prompt Compiler</span>
          <span className="chip">Ollama</span>
          <span className="chip">Agent Loop</span>
        </div>
      </section>
    </div>
  )
}

export default Settings

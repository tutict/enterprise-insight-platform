const templates = [
  {
    name: 'harness-default',
    sections: 'ROLE, GOAL, MODULES, CONSTRAINTS, OUTPUT FORMAT',
    purpose: 'Stable Spring Boot project generation.',
  },
  {
    name: 'repair-feedback',
    sections: 'REPAIR CONTEXT, VERIFICATION ERROR, PREVIOUS OUTPUT',
    purpose: 'Feeds compiler errors back into the agent loop.',
  },
  {
    name: 'file-block-output',
    sections: 'FILE START, path, content, FILE END',
    purpose: 'Forces generated projects into writable file blocks.',
  },
]

function Insights() {
  return (
    <div className="templates-page">
      <section className="glass-card template-builder">
        <div>
          <p className="eyebrow">Template system</p>
          <h2>Prompt templates are compiler targets.</h2>
          <p className="muted">
            Templates define stable output contracts for local agents and
            automatic repair loops.
          </p>
        </div>
        <div className="query-panel">
          <textarea
            rows={4}
            defaultValue="# ROLE&#10;You are an AI Harness coding agent.&#10;&#10;# OUTPUT FORMAT&#10;Return file blocks only."
          />
          <div className="query-actions">
            <button className="ghost-btn" type="button">
              Validate
            </button>
            <button className="primary-btn" type="button">
              Save template
            </button>
          </div>
        </div>
      </section>

      <section className="template-grid">
        {templates.map((template, index) => (
          <article
            key={template.name}
            className="glass-card template-card"
            style={{ animationDelay: `${index * 120}ms` }}
          >
            <div className="card-header">
              <h3>{template.name}</h3>
              <span className="chip">active</span>
            </div>
            <p>{template.purpose}</p>
            <p className="muted">{template.sections}</p>
          </article>
        ))}
      </section>
    </div>
  )
}

export default Insights

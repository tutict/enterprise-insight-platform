<template>
  <section class="home">
    <!-- 主视觉区：标题与入口 -->
    <div class="hero">
      <p class="eyebrow">Enterprise Insight Platform</p>
      <h1>Enterprise Analytics & AI Assistant MVP</h1>
      <p class="subtitle">
        Resume-ready MVP: Nacos + Spring Cloud microservices + Garfish micro-frontends + Mock AI agent.
      </p>
      <div class="actions">
        <router-link to="/insight" class="primary">Open Insight</router-link>
        <router-link to="/assistant" class="ghost">Open Assistant</router-link>
      </div>
    </div>
    <!-- 功能卡片区 -->
    <div class="grid">
      <div class="card">
        <h3>Metric Management</h3>
        <p>Metric cards, trend badges, and business definitions ready for expansion.</p>
      </div>
      <div class="card">
        <h3>Natural Language Query</h3>
        <p>Mock agent suggests metrics and SQL for fast iteration before real LLM.</p>
      </div>
      <div class="card">
        <h3>Multi-tenant Governance</h3>
        <p>Auth, roles, and audit skeleton prepared for enterprise rollout.</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 12px;
  color: var(--muted);
}

.subtitle {
  max-width: 640px;
  color: var(--muted);
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary,
.ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 10px 18px;
  font-weight: 600;
  text-decoration: none;
}

.primary {
  background: var(--accent);
  color: #0b0b0f;
}

.ghost {
  border: 1px solid var(--border);
  color: var(--text);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.card {
  padding: 18px;
  border-radius: 16px;
  background: var(--panel);
  border: 1px solid var(--border);
  min-height: 140px;
}

.card h3 {
  margin: 0 0 8px;
}

.card p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
}
</style>

<template>
  <div class="app">
    <header class="topbar">
      <!-- 顶部品牌与导航 -->
      <div class="brand">
        <div class="brand-mark">EI</div>
        <div>
          <h1>Enterprise Insight</h1>
          <p>Microservices + Garfish</p>
        </div>
      </div>
      <nav class="nav">
        <router-link to="/">Overview</router-link>
        <router-link to="/insight">Insight</router-link>
        <router-link to="/assistant">AI Assistant</router-link>
      </nav>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.topbar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px 0 8px;
  border-bottom: 1px solid var(--border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 700;
  background: var(--accent);
  color: #0b0b0f;
}

.brand h1 {
  margin: 0;
  font-size: 20px;
}

.brand p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.nav {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.nav a {
  text-decoration: none;
  color: var(--text);
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
}

.nav a.router-link-active {
  border-color: var(--border);
  background: var(--panel);
}

.content {
  padding-bottom: 40px;
}

@media (min-width: 900px) {
  .topbar {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

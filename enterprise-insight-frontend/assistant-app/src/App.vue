<template>
  <div class="app">
    <!-- 左侧导航 + 右侧内容 -->
    <aside class="side">
      <h2>AI Agent</h2>
      <p class="tag">Mock Copilot</p>
      <nav>
        <router-link to="/">Chat</router-link>
        <router-link to="/prompts">Prompt Library</router-link>
      </nav>
    </aside>
    <section class="content">
      <router-view />
    </section>
  </div>
</template>

<style scoped>
.app {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
}

.side {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 16px;
  height: fit-content;
}

.side h2 {
  margin: 0 0 4px;
}

.tag {
  margin: 0 0 16px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.6);
}

nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

nav a {
  color: inherit;
  text-decoration: none;
  padding: 8px 10px;
  border-radius: 12px;
}

nav a.router-link-active {
  background: rgba(100, 255, 191, 0.18);
  color: #9bffd1;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 900px) {
  .app {
    grid-template-columns: 1fr;
  }
}
</style>

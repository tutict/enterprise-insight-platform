<template>
  <section class="subapp">
    <!-- 子应用标题与说明 -->
    <header class="subapp-header">
      <h2>{{ title }}</h2>
      <p>Loaded by Garfish micro-frontend.</p>
    </header>
    <!-- Garfish 子应用挂载容器 -->
    <div id="subapp-container" class="subapp-container"></div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: 'Sub Application',
  },
})
</script>

<style scoped>
.subapp {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.subapp-header {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.subapp-header p {
  margin: 0;
  color: var(--muted);
}

.subapp-container {
  min-height: 60vh;
  border-radius: 16px;
  border: 1px dashed var(--border);
  background: rgba(255, 255, 255, 0.02);
  padding: 16px;
}
</style>

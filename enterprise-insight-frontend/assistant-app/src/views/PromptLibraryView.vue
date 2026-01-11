<template>
  <section class="panel">
    <!-- Prompt 列表 -->
    <h1>Prompt Library</h1>
    <p class="subtitle">Reusable prompts for data analysis and KPI explanation.</p>
    <ul>
      <!-- Prompt 行 -->
      <li v-for="prompt in prompts" :key="prompt.title">
        <h3>{{ prompt.title }}</h3>
        <p>{{ prompt.text }}</p>
      </li>
    </ul>
  </section>
</template>

<script setup>
// Mock Prompt 数据
const prompts = [
  {
    title: 'Weekly performance summary',
    text: 'Summarize revenue, orders, and conversion rate for the last 7 days.',
  },
  {
    title: 'Channel comparison',
    text: 'Compare conversion rate and ROI across acquisition channels.',
  },
  {
    title: 'Anomaly detection',
    text: 'Find anomalies in daily revenue and explain possible causes.',
  },
]
</script>

<style scoped>
.panel {
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.subtitle {
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 16px 0 0;
  display: grid;
  gap: 12px;
}

li {
  padding: 12px;
  border-radius: 12px;
  background: rgba(10, 12, 18, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

li h3 {
  margin: 0 0 6px;
}

li p {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
}
</style>

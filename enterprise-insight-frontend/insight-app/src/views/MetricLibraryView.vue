<template>
  <section class="panel">
    <!-- 指标口径列表 -->
    <h1>Metric Library</h1>
    <p class="subtitle">Standardized metrics with definitions and owners.</p>
    <table>
      <thead>
        <tr>
          <th>Metric</th>
          <th>Owner</th>
          <th>Update</th>
        </tr>
      </thead>
      <tbody>
        <!-- 指标行 -->
        <tr v-for="metric in metrics" :key="metric.name">
          <td>
            <div class="name">{{ metric.name }}</div>
            <div class="desc">{{ metric.desc }}</div>
          </td>
          <td>{{ metric.owner }}</td>
          <td>{{ metric.refresh }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
// Mock 指标口径数据
const metrics = [
  { name: 'Revenue', desc: 'Net sales after refunds', owner: 'Finance', refresh: 'Daily' },
  { name: 'Conversion Rate', desc: 'Orders / sessions', owner: 'Growth', refresh: 'Hourly' },
  { name: 'Retention', desc: '30-day active users', owner: 'Product', refresh: 'Weekly' },
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

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}

th,
 td {
  text-align: left;
  padding: 10px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 14px;
}

th {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 255, 255, 0.6);
}

.name {
  font-weight: 600;
}

.desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}
</style>

<template>
  <section class="panel">
    <!-- 指标概览标题 -->
    <h1>Revenue Overview</h1>
    <p class="subtitle">Mock metrics from analysis-service</p>
    <!-- 指标卡片列表 -->
    <div class="cards">
      <div class="card" v-for="metric in metrics" :key="metric.name">
        <div class="card-title">{{ metric.name }}</div>
        <div class="card-value">{{ metric.value }} <span>{{ metric.unit }}</span></div>
        <div class="card-trend">{{ metric.trend }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
// Mock 指标数据
const metrics = [
  { name: 'Revenue', value: '1.28', unit: 'M', trend: '+12%' },
  { name: 'Orders', value: '3,560', unit: 'orders', trend: '+4.1%' },
  { name: 'AOV', value: '361', unit: 'CNY', trend: '-1.3%' },
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

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.card {
  padding: 14px;
  border-radius: 12px;
  background: rgba(10, 12, 18, 0.6);
}

.card-title {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.card-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 6px;
}

.card-value span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.card-trend {
  margin-top: 8px;
  font-size: 12px;
  color: #9bffd1;
}
</style>

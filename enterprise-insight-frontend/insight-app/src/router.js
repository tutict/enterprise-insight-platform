import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from './views/DashboardView.vue'
import MetricLibraryView from './views/MetricLibraryView.vue'

// 洞察子应用路由工厂
export const createInsightRouter = (basename = '/') =>
  createRouter({
    history: createWebHistory(basename),
    routes: [
      { path: '/', name: 'dashboard', component: DashboardView },
      { path: '/metrics', name: 'metrics', component: MetricLibraryView },
    ],
  })

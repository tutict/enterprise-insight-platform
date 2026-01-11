import { createRouter, createWebHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import SubAppView from './views/SubAppView.vue'

// 主应用路由
const routes = [
  { path: '/', name: 'home', component: HomeView },
  {
    path: '/insight/:pathMatch(.*)*',
    name: 'insight',
    component: SubAppView,
    props: { title: '数据洞察子应用' },
  },
  {
    path: '/assistant/:pathMatch(.*)*',
    name: 'assistant',
    component: SubAppView,
    props: { title: 'AI 助手子应用' },
  },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})

import { createRouter, createWebHistory } from 'vue-router'
import ChatView from './views/ChatView.vue'
import PromptLibraryView from './views/PromptLibraryView.vue'

// 助手子应用路由工厂
export const createAssistantRouter = (basename = '/') =>
  createRouter({
    history: createWebHistory(basename),
    routes: [
      { path: '/', name: 'chat', component: ChatView },
      { path: '/prompts', name: 'prompts', component: PromptLibraryView },
    ],
  })

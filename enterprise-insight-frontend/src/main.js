import { createApp } from 'vue'
import Garfish from 'garfish'
import './style.css'
import App from './App.vue'
import router from './router'

// 创建主应用实例
const app = createApp(App)
app.use(router)
app.mount('#app')

// 路由就绪后启动 Garfish 子应用
router.isReady().then(() => {
  Garfish.run({
    domGetter: () => document.querySelector('#subapp-container'),
    apps: [
      {
        name: 'insight-app',
        entry: 'http://localhost:5174',
        activeWhen: '/insight',
        sandbox: false,
      },
      {
        name: 'assistant-app',
        entry: 'http://localhost:5175',
        activeWhen: '/assistant',
        sandbox: false,
      },
    ],
  })
})

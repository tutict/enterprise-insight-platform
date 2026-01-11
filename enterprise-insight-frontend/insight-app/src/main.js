import { createApp, h } from 'vue'
import { vueBridge } from '@garfish/bridge-vue-v3'
import App from './App.vue'
import { createInsightRouter } from './router'
import './style.css'

// Garfish 子应用桥接
export const provider = vueBridge({
  rootComponent: App,
  appOptions: ({ basename }) => ({
    el: '#app',
    render: () => h(App),
    router: createInsightRouter(basename),
  }),
})

// 独立运行时直接挂载
if (!window.__GARFISH__) {
  const app = createApp(App)
  app.use(createInsightRouter('/'))
  app.mount('#app')
}

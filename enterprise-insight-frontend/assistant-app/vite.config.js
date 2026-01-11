import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: 'http://localhost:5175/',
  server: {
    port: 5175,
    cors: true,
    origin: 'http://localhost:5175',
  },
})

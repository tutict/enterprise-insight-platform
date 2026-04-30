import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_API_BASE_URL || 'http://localhost:8080'
  const base = env.VITE_PUBLIC_PATH || '/'
  const isProd = mode === 'production'

  return {
    base,
    plugins: [react()],
    server: {
      proxy: {
        '/api': target,
        '/actuator': target,
      },
    },
    build: {
      target: 'es2019',
      cssCodeSplit: true,
      sourcemap: false,
      assetsInlineLimit: 4096,
      rollupOptions: {
        output: {
          manualChunks: {
            react: ['react', 'react-dom'],
            router: ['react-router-dom'],
          },
        },
      },
    },
    esbuild: isProd
      ? { drop: ['console', 'debugger'] }
      : undefined,
  }
})

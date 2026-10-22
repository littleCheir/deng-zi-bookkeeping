import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发期通过 Vite 代理把 /api 转发到 Spring Boot，保证浏览器视角下前后端同源：
// - Cookie（Session）与 CSRF Cookie 的语义和生产环境一致
// - 端口/代理目标都可用环境变量覆盖，不写死在代码里
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const devPort = Number(env.VITE_DEV_PORT ?? 5173)
  const proxyTarget = env.VITE_PROXY_TARGET ?? 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: devPort,
      // 允许手机在同一局域网访问开发服务器
      host: true,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: false
        }
      }
    }
  }
})

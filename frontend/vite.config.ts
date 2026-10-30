import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

// 开发期通过 Vite 代理把 /api 转发到 Spring Boot，保证浏览器视角下前后端同源：
// - Cookie（Session）与 CSRF Cookie 的语义和生产环境一致
// - 端口/代理目标都可用环境变量覆盖，不写死在代码里
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const devPort = Number(env.VITE_DEV_PORT ?? 5173)
  const proxyTarget = env.VITE_PROXY_TARGET ?? 'http://localhost:8080'

  return {
    plugins: [
      vue(),
      VitePWA({
        // 只在生产构建生成 SW，开发环境不启用，避免旧缓存干扰开发
        devOptions: { enabled: false },
        registerType: 'prompt',
        // 注册逻辑由 PwaUpdatePrompt.vue 通过 virtual:pwa-register/vue 接管
        injectRegister: false,
        manifest: {
          id: '/',
          name: '凳子记账',
          short_name: '凳子记账',
          description: '凳子记账：个人收入与支出记录',
          lang: 'zh-CN',
          start_url: '/',
          scope: '/',
          display: 'standalone',
          background_color: '#ffffff',
          theme_color: '#ffffff',
          icons: [
            {
              src: '/icons/icon-192.png',
              sizes: '192x192',
              type: 'image/png',
              purpose: 'any'
            },
            {
              src: '/icons/icon-512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any'
            },
            {
              src: '/icons/icon-512-maskable.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'maskable'
            }
          ]
        },
        workbox: {
          // 只预缓存本应用构建产物；API 请求不会进入这里
          globPatterns: ['**/*.{js,css,html,ico,png,svg,webmanifest}'],
          // 主包包含 Element Plus 与 ECharts，放宽单文件上限避免构建被拦
          maximumFileSizeToCacheInBytes: 4 * 1024 * 1024,
          navigateFallback: '/index.html',
          // 双重保险：API 导航/请求不得回退到 index.html
          navigateFallbackDenylist: [/^\/api\//],
          cleanupOutdatedCaches: true
        }
      })
    ],
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
    },
    // 本地验收生产构建时使用：npm run build && npm run preview
    // preview 不会继承 server.proxy，这里单独配置一份
    preview: {
      port: 4173,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: false
        }
      }
    }
  }
})

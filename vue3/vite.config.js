import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  optimizeDeps: {
    include: [
      '@wangeditor/editor',
      '@wangeditor/editor-for-vue'
    ],
    force: true
  },
  build: {
    commonjsOptions: {
      include: [/@wangeditor\/.*/, /node_modules/]
    }
  },
  server: {
    port: 8800,
    open: true,
    compress: false,
    proxy: {
      '/api': {
        target: 'http://localhost:1235',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/files': {
        target: 'http://localhost:1235',
        changeOrigin: true,
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ["legacy-js-api"],
        // 移除自动注入，防止循环导入
        api: 'modern-compiler' // 使用新的 Sass API
      }
    }
  },
  // 定义环境变量
  define: {
    'import.meta.env.VITE_APP_BASE_API': JSON.stringify('/api')
  }
}) 
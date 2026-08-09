import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [
        ElementPlusResolver({
          importStyle: 'css'
        })
      ]
    })
  ],
  build: {
    chunkSizeWarningLimit: 650,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return

          if (id.includes('vue-router')) return 'router'
          if (id.includes('pinia')) return 'store'
          if (id.includes('axios')) return 'request'
          if (id.includes('@element-plus/icons-vue')) return 'element-icons'
          if (id.includes('vue')) return 'vue-core'
        }
      }
    }
  }
})

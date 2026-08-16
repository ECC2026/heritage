import { cpSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

function copyStaticAssets() {
  return {
    name: 'copy-static-assets',
    apply: 'build',
    closeBundle() {
      const inputDir = resolve(process.env.UNI_INPUT_DIR || '.')
      const outputDir = resolve(process.env.UNI_OUTPUT_DIR)
      const staticDir = resolve(inputDir, 'static')

      if (existsSync(staticDir)) {
        cpSync(staticDir, resolve(outputDir, 'static'), { recursive: true })
      }
    }
  }
}

export default defineConfig({
  plugins: [uni(), copyStaticAssets()]
})

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path';

export default defineConfig({
    // root: path.resolve(__dirname, 'src'),
    plugins: [vue()],
    resolve: {
        alias: {
            '~bootstrap': path.resolve(__dirname, 'node_modules/bootstrap'),
        }
    },
    server: {
      proxy: {
        '/api': {
            target: 'http://localhost:8080/',
            changeOrigin: true,
            secure: false,
            rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
})

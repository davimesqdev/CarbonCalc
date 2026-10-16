import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// O backend sobe na 8085 por padrão (veja backend/src/main/resources/application.yml).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8085',
    },
  },
});

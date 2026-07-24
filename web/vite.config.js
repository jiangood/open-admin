import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import openAdmin from './vite-plugin/index.js';

export default defineConfig({
    plugins: [react(), openAdmin()],
});
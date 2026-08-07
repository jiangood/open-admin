import {FullConfig} from '@playwright/test';

async function globalSetup(_config: FullConfig) {
    console.log('  ✅ Services should be started by webServer config');
    console.log('  🌐 Frontend: ' + (process.env.BASE_URL || 'http://localhost:3456'));
    console.log('  🔧 Backend API: ' + (process.env.API_BASE || 'http://localhost:8910'));
}

export default globalSetup;

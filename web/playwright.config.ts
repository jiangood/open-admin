import {defineConfig} from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:3456';

export default defineConfig({
    testDir: './e2e',
    fullyParallel: false,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 1 : 0,
    workers: 1,
    reporter: process.env.CI ? 'github' : 'list',
    timeout: 60000,
    use: {
        baseURL: BASE_URL,
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
    },
    globalSetup: './e2e/global-setup.ts',
    webServer: [
        {
            command: 'cd .. && mvn spring-boot:run -Dspring-boot.run.profiles=lib,e2e -Dspring-boot.run.arguments=--server.port=8910 -q',
            port: 8910,
            reuseExistingServer: !process.env.CI,
            timeout: 120000,
        },
        {
            command: 'npm run dev',
            port: 3456,
            reuseExistingServer: !process.env.CI,
            timeout: 30000,
        },
    ],
    projects: [
        {
            name: 'setup',
            testMatch: /auth\.setup\.ts/,
        },
        {
            name: 'noauth',
            testMatch: '**/login.spec.ts',
            dependencies: ['setup'],
        },
        {
            name: 'app',
            testMatch: '**/!(login).spec.ts',
            dependencies: ['setup'],
            use: {
                storageState: 'e2e/.auth/user.json',
            },
        },
    ],
});

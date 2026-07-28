import {request} from '@playwright/test';

const BACKEND = process.env.API_BASE || 'http://localhost:8080';
const CONTEXT_PATH = process.env.SERVLET_CONTEXT || '/change-this-servlet-context';
const BASE = BACKEND + CONTEXT_PATH;

function encodePassword(pwd: string): string {
    const chars: number[] = [];
    for (let i = 0; i < pwd.length; i++)
        chars.push(pwd.charCodeAt(i) + i + 2);
    return btoa(String.fromCharCode(...chars));
}

async function loginCtx() {
    const ctx = await request.newContext();
    await ctx.post(`${BASE}/admin/auth/login`, {
        data: {username: 'admin', password: encodePassword('Open@1234')},
    });
    return ctx;
}

export interface TestUser {
    name: string;
    account: string;
    password?: string;
    phone?: string;
    email?: string;
    enabled?: boolean;
}

export async function createTestUser(data: TestUser): Promise<string> {
    const ctx = await loginCtx();
    const res = await ctx.post(`${BASE}/admin/sysUser/create`, {
        data: {
            name: data.name,
            account: data.account,
            password: data.password || '123456',
            phone: data.phone,
            email: data.email,
            enabled: data.enabled ?? true,
        },
    });
    const body = await res.json();
    if (!body.success) throw new Error(`创建用户失败: ${body.message}`);

    const pageRes = await ctx.get(`${BASE}/admin/sysUser/page`, {
        params: {account: data.account, size: 1},
    });
    const pageBody = await pageRes.json();
    const users = pageBody.data?.content || [];
    if (users.length === 0) throw new Error('创建后未找到用户');
    return users[0].id;
}

export async function deleteTestUser(id: string) {
    const ctx = await loginCtx();
    await ctx.post(`${BASE}/admin/sysUser/delete`, {
        data: {id},
    });
}

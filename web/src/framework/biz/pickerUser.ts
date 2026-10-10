/** 选人器使用到的用户信息 */
export interface PickerUser {
    id: string;
    name?: string;
    account?: string;
    phone?: string;
    orgLabel?: string;
    unitLabel?: string;
    [key: string]: unknown;
}

/** 用户标签文案：姓名（账号 · 机构），用于区分同名用户 */
export function formatUserLabel(user: PickerUser): string {
    const extras = [user.account, user.orgLabel || user.unitLabel].filter(Boolean) as string[];
    if (extras.length === 0) {
        return user.name || user.id;
    }
    return `${user.name || ''}（${extras.join(' · ')}）`;
}

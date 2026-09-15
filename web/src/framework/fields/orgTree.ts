export function orgTreeUrl(type: string): string {
    return type === 'dept' ? '/admin/sysOrg/dept-tree' : '/admin/sysOrg/unit-tree';
}

export function orgTreeUrl(type: string): string {
    return type === 'dept' ? '/admin/sys-org/dept-tree' : '/admin/sys-org/unit-tree';
}

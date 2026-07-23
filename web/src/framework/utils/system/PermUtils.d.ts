export class PermUtils {
    static hasPermission(perm: string | null | undefined): boolean;
    static isPermitted(p: string | null | undefined): boolean;
    static notPermitted(p: string | null | undefined): boolean;
}

import {GlobalData} from '../GlobalData';
import {ArrayUtils} from './ArrayUtils';

export class PermUtils {
    static getPermissions(): string[] {
        const { permissions } = GlobalData.getLoginInfo();
        if (permissions == null || permissions.length === 0) return [];
        return permissions;
    }

    static hasPermission(perm: string | null | undefined): boolean {
        if (perm === null || perm === undefined || perm === '') return false;
        const permissions = PermUtils.getPermissions();
        if (permissions.length === 0) return false;
        if (ArrayUtils.contains(permissions, "*")) return true;
        return permissions.indexOf(perm) > -1;
    }

    static isPermitted(p: string | null | undefined): boolean {
        return PermUtils.hasPermission(p);
    }

    static notPermitted(p: string | null | undefined): boolean {
        return !PermUtils.isPermitted(p);
    }
}

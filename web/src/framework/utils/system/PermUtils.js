import {GlobalData} from '../../GlobalData';
import {ArrayUtils} from '../ArrayUtils';

export class PermUtils {
    static getPermissions() {
        const info = GlobalData.getLoginInfo();
        const { permissions } = info;
        if (permissions == null || permissions.length === 0) return [];
        return permissions;
    }

    static hasPermission(perm) {
        if (perm === null || perm === undefined || perm === '') return false;
        const permissions = PermUtils.getPermissions();
        if (permissions.length === 0) return false;
        if (ArrayUtils.contains(permissions, "*")) return true;
        return permissions.indexOf(perm) > -1;
    }

    static isPermitted(p) {
        return PermUtils.hasPermission(p);
    }

    static notPermitted(p) {
        return !PermUtils.isPermitted(p);
    }
}

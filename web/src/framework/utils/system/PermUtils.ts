import {SysUtils} from "./SysUtils";
import {ArrUtils} from "../ArrUtils";

/**
 * 登录用户信息接口，
 */
interface LoginInfo {
  // ... 其他登录信息字段
  permissions: string[];
}

/**
 * 权限控制工具类
 */
export class PermUtils {

  /**
   * 获取当前用户的权限列表信息。
   * @private
   * @returns {string[]} 权限码数组。
   */
  private static getPermissions(): string[] {
    // 假设 SysUtil.getLoginInfo() 返回 LoginInfo 类型的对象
    const info: LoginInfo = SysUtils.getLoginInfo();
    const { permissions } = info;

    if (permissions == null || permissions.length === 0) {
      return [];
    }

    return permissions;
  }

  /**
   * 是否拥有权限
   * * @param perm 权限码 (string)
   * @returns {boolean} 如果拥有权限则为 true，否则为 false。
   */
  public static hasPermission(perm: string | null | undefined): boolean {
    if (perm === null || perm === undefined || perm === '') {
      return false;
    }

    const permissions: string[] = PermUtils.getPermissions();

    if (permissions.length === 0) {
      return false;
    }

    // 检查是否拥有超级权限 '*'
    // 假设 ArrUtils.contains(array, item) 存在且返回 boolean
    if (ArrUtils.contains(permissions, "*")) {
      return true;
    }

    // 检查是否包含指定的权限码
    return permissions.indexOf(perm) > -1;
  }

  /**
   * 是否授权，功能上与 hasPermission 相同。
   * * @param p 权限码 (string)
   * @returns {boolean} 如果拥有权限则为 true，否则为 false。
   */
  public static isPermitted(p: string | null | undefined): boolean {
    // 直接调用内部方法
    return PermUtils.hasPermission(p);
  }

  /**
   * 是否没有权限。
   * * @param p 权限码 (string)
   * @returns {boolean} 如果没有权限则为 true，否则为 false。
   */
  public static notPermitted(p: string | null | undefined): boolean {
    // 使用!取反，并确保方法返回布尔值
    return !PermUtils.isPermitted(p);
  }
}

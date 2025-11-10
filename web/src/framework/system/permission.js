/**
 * 控制按钮是否显示
 */
import {SysUtil} from "./sys";
import {ArrUtil} from "@tmgg/tmgg-commons-lang";


export const PermUtil = {

  /**
   * 是否拥有权限
   * @param perm 权限码
   */
  hasPermission(perm) {
    if (perm === null || perm === '') {
      return false;
    }

    const info = SysUtil.getLoginInfo();
    const { permissions } = info;
    if(permissions == null || permissions.length === 0){
      return false
    }

    if (ArrUtil.contains(permissions,"*")) {
      return true;
    }

    return permissions.indexOf(perm) > -1;
  },

  /**
   * 是否授权， 同 hasPermission
   * @param p
   * @returns {boolean}
   */
    isPermitted(p){
    return  PermUtil.hasPermission(p)
  },
    notPermitted(p){
      !PermUtil.isPermitted(p)
  }

}

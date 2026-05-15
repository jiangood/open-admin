import React from 'react';
import { Space } from 'antd';
import { PermUtils } from "../../utils";

/**
 * 带权限的按钮列表
 * @param maxNum: 显示子节点的个数， 超过的为收缩起来
 */
export function ButtonList(props) {
  const { children } = props;

  // 检查权限
  const checkPerm = (element) => {
    const _props = element?.props;
    return _props == null || _props.perm == null || PermUtils.hasPermission(_props.perm);
  };

  // 单节点情况
  if (!Array.isArray(children)) {
    return checkPerm(children) ? children : null;
  }

  const menus = [];
  for (let child of children) {
    if (child == null) continue;
    if (checkPerm(child)) menus.push(child);
  }
  return <Space>{menus}</Space>;
}

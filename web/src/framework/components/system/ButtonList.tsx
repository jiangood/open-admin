import React from 'react';
import { Space } from 'antd';
import { PermUtils } from '../../utils';

/**
 * 带权限的按钮列表
 * @param maxNum: 显示子节点的个数， 超过的为收缩起来
 */
export function ButtonList(props) {
  const { children } = props;

  const checkPerm = (element) => {
    const _props = element?.props;
    return !_props?.perm || PermUtils.hasPermission(_props.perm);
  };

  const nodes = React.Children.toArray(children).filter(
    (child) => child != null && checkPerm(child)
  );

  return <Space>{nodes}</Space>;
}

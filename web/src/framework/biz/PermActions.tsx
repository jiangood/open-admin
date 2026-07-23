import React from 'react';
import { Space } from 'antd';
import { PermUtils } from '../utils';

/**
 * 带权限的操作区。根据子元素的 `perm` 属性控制显隐，默认用 Space 包裹。
 * @param gap - 是否在操作项之间增加间距，默认 true
 */
export function PermActions(props) {
  const { children, gap = true } = props;

  const checkPerm = (element) => {
    const _props = element?.props;
    return !_props?.perm || PermUtils.hasPermission(_props.perm);
  };

  const nodes = React.Children.toArray(children).filter(
    (child) => child != null && checkPerm(child)
  );

  if (!gap) return <>{nodes}</>;
  return <Space>{nodes}</Space>;
}

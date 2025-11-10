import React from 'react';
import { PermUtil} from "../system";

/**
 * 使用该组件，可以判断权限
 * @param
 */
export class HasPerm extends React.Component {

  render() {
    let {code} = this.props;

    if (PermUtil.hasPermission(code)) {
      return this.props.children;
    }

  }
}

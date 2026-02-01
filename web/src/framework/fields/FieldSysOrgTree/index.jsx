/**
 * 组织机构树
 */
import React from "react";
import {FieldRemoteTree} from "../FieldRemoteTree";

export class FieldSysOrgTree extends React.Component {
  static defaultProps = {
    type: 'dept',
  };

  render() {
    const {type, ...rest} = this.props;
    const url = type === 'dept'?
        '/admin/sysOrg/deptTree':
        '/admin/sysOrg/unitTree';
    return <FieldRemoteTree url={url} {...rest} />;
  }

}
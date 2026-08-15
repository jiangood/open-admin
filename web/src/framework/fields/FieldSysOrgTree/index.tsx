/**
 * 组织机构树
 */
import React from "react";
import {FieldRemoteTree} from "../FieldRemoteTree";
import type {FieldProps} from '../types';

interface FieldSysOrgTreeProps extends FieldProps<string[]> {
    type?: string;
}

export class FieldSysOrgTree extends React.Component<FieldSysOrgTreeProps> {
  static readonly defaultProps = {
    type: 'dept',
  };

  render() {
    const {type, ...rest} = this.props;
    const url = type === 'dept'?
        '/admin/sysOrg/dept-tree':
        '/admin/sysOrg/unit-tree';
    return <FieldRemoteTree url={url} {...rest} />;
  }

}
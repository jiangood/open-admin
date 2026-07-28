/**
 * 组织机构树选择器
 */
import React from "react";
import {FieldRemoteTreeSelect} from "../FieldRemoteTreeSelect";
import type {FieldProps} from '../types';

interface FieldSysOrgTreeSelectProps extends FieldProps<string | string[]> {
    type?: string;
}

export class FieldSysOrgTreeSelect extends React.Component<FieldSysOrgTreeSelectProps> {

    static defaultProps = {
        type: 'dept',
    };

    render() {
        const {type, ...rest} = this.props;
        const url = type === 'dept'?
            '/admin/sysOrg/dept-tree':
            '/admin/sysOrg/unit-tree';

        return <FieldRemoteTreeSelect url={url} {...rest}/>;
    }

}
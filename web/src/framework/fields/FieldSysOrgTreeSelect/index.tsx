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
        let url = '/admin/sysOrg/dept-tree';
        if (type === 'unit') {
            url = '/admin/sysOrg/unit-tree';
        } else if (type === 'shop') {
            url = '/admin/sysOrg/tree?type=3';
        }

        return <FieldRemoteTreeSelect url={url} {...rest}/>;
    }
}

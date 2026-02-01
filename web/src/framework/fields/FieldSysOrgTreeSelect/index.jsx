/**
 * 组织机构树选择器
 */
import React from "react";
import {FieldRemoteTreeSelect} from "../FieldRemoteTreeSelect";

export class FieldSysOrgTreeSelect extends React.Component {

    static defaultProps = {
        type: 'dept',
    };

    render() {
        const {type, ...rest} = this.props;
        const url = type === 'dept'?
            '/admin/sysOrg/deptTree':
            '/admin/sysOrg/unitTree';

        return <FieldRemoteTreeSelect url={url} {...rest}/>;
    }

}
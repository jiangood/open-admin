import {FieldRemoteSelect, FieldRemoteSelectMultiple, FieldRemoteTreeSelect} from "../fields";
import React from "react";

export * from './PermActions'
export * from './HasPerm'
export * from './OrgTree'
export * from './RoleTree'

export function FieldUserSelect(props) {
    return <FieldRemoteSelect url="admin/sysUser/options" {...props} />;
}
export function FieldUserSelectMultiple(props) {
    return <FieldRemoteSelectMultiple url="admin/sysUser/options" {...props} />;
}

export function FieldUnitTreeSelect(props) {
    return <FieldRemoteTreeSelect url="admin/sysOrg/unit-tree" {...props} />;
}

export function FieldDeptTreeSelect(props) {
    return <FieldRemoteTreeSelect url="admin/sysOrg/dept-tree" {...props} />;
}

export function FieldOrgTreeSelect(props) {
    return <FieldRemoteTreeSelect url="admin/sysOrg/dept-tree" {...props} />;
}

export function FieldOrgTreeMultipleSelect(props) {
    return <FieldRemoteSelectMultiple url="/sysOrg/dept-tree" {...props} />;
}


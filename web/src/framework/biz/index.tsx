/* eslint-disable react-refresh/only-export-components */
import {FieldRemoteSelect, FieldRemoteTreeSelect} from "../fields";
import React, {type ComponentProps} from "react";

export * from './PermActions'
export * from './Perm'
export * from './OrgTree'
export * from './RoleTree'

class InnerFieldUserSelect extends React.Component<ComponentProps<typeof FieldRemoteSelect>> {
    render() {
        return <FieldRemoteSelect url="admin/sysUser/options" {...this.props} />;
    }
}
class InnerFieldUserSelectMultiple extends React.Component<ComponentProps<typeof FieldRemoteSelect>> {
    render() {
        return <FieldRemoteSelect url="admin/sysUser/options" multiple {...this.props} />;
    }
}
class InnerFieldUnitTreeSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sysOrg/unit-tree" {...this.props} />;
    }
}
class InnerFieldDeptTreeSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sysOrg/dept-tree" {...this.props} />;
    }
}
class InnerFieldOrgTreeSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sysOrg/dept-tree" {...this.props} />;
    }
}
class InnerFieldOrgTreeMultipleSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sysOrg/dept-tree" multiple {...this.props} />;
    }
}

export const FieldUserSelect = InnerFieldUserSelect;
export const FieldUserSelectMultiple = InnerFieldUserSelectMultiple;
export const FieldUnitTreeSelect = InnerFieldUnitTreeSelect;
export const FieldDeptTreeSelect = InnerFieldDeptTreeSelect;
export const FieldOrgTreeSelect = InnerFieldOrgTreeSelect;
export const FieldOrgTreeMultipleSelect = InnerFieldOrgTreeMultipleSelect;


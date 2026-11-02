/* eslint-disable react-refresh/only-export-components */
import {FieldRemoteSelect, FieldRemoteTreeSelect} from "../fields";
import React, {type ComponentProps} from "react";

export * from './PermActions'
export * from './Perm'
export * from './OrgTree'
export * from './RoleTree'
export * from './UserPicker'
export * from './pickerUser'

class InnerFieldUserSelect extends React.Component<ComponentProps<typeof FieldRemoteSelect>> {
    render() {
        return <FieldRemoteSelect url="admin/sys-user/options" {...this.props} />;
    }
}
class InnerFieldUserSelectMultiple extends React.Component<ComponentProps<typeof FieldRemoteSelect>> {
    render() {
        return <FieldRemoteSelect url="admin/sys-user/options" multiple {...this.props} />;
    }
}
class InnerFieldUnitTreeSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sys-org/unit-tree" {...this.props} />;
    }
}
class InnerFieldDeptTreeSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <FieldRemoteTreeSelect url="admin/sys-org/dept-tree" {...this.props} />;
    }
}
class InnerFieldOrgTreeMultipleSelect extends React.Component<ComponentProps<typeof FieldRemoteTreeSelect>> {
    render() {
        return <InnerFieldDeptTreeSelect multiple {...this.props} />;
    }
}

export const FieldUserSelect = InnerFieldUserSelect;
export const FieldUserSelectMultiple = InnerFieldUserSelectMultiple;
export const FieldUnitTreeSelect = InnerFieldUnitTreeSelect;
export const FieldDeptTreeSelect = InnerFieldDeptTreeSelect;
/** FieldDeptTreeSelect 的别名 */
export const FieldOrgTreeSelect = InnerFieldDeptTreeSelect;
export const FieldOrgTreeMultipleSelect = InnerFieldOrgTreeMultipleSelect;


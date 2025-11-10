import React from 'react';
import { FieldRemoteTreeSelect, FieldSelect} from './field';


export function FieldUserSelect(props) {
  return <FieldSelect url="/sysUser/options" {...props} />;
}

export function FieldUserMultipleSelect(props) {
  return <FieldSelect multiple={true} url="/sysUser/options" {...props} />;
}

export function FieldUnitTreeSelect(props) {
  return <FieldRemoteTreeSelect url="/sysOrg/unitTree" {...props} />;
}

export function FieldDeptTreeSelect(props) {
  return <FieldRemoteTreeSelect url="/sysOrg/deptTree" {...props} />;
}

export function FieldOrgTreeSelect(props) {
  return <FieldRemoteTreeSelect url="/sysOrg/deptTree" {...props} />;
}

export function FieldOrgTreeMultipleSelect(props) {
  return <FieldSelect multiple={true} url="/sysOrg/deptTree" {...props} />;
}



import React, { useState } from 'react';
import {
  FieldRemoteSelect,
  FieldRemoteSelectMultiple,
  FieldRemoteSelectMultipleInline,
  FieldRemoteTreeSelect,
  FieldRemoteTreeSelectMultiple,
  FieldRemoteTree,
  FieldRemoteTreeCascader,
  FieldSysOrgTree,
  FieldSysOrgTreeSelect,
} from "../../framework";

function Section({ title, children }) {
  return (
    <div style={{ marginBottom: 32, padding: 16, border: '1px solid #f0f0f0', borderRadius: 8 }}>
      <h3 style={{ marginTop: 0, marginBottom: 16 }}>{title}</h3>
      {children}
    </div>
  );
}

function FieldRemoteSelectTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldRemoteSelect — 远程搜索单选（字典类型选项）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteSelect url="/admin/dict/typeOptions" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteSelectMultipleTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldRemoteSelectMultiple — 远程搜索多选（角色选项）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteSelectMultiple url="/admin/sysRole/options" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteSelectMultipleInlineTest() {
  const [value, setValue] = useState('1,2');
  return (
    <Section title="FieldRemoteSelectMultipleInline — 多选逗号字符串值">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteSelectMultipleInline url="/admin/sysRole/options" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteTreeSelectTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldRemoteTreeSelect — 远程树下拉单选（组织树）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteTreeSelect url="/admin/sysOrg/tree" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteTreeSelectMultipleTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldRemoteTreeSelectMultiple — 远程树下拉多选（组织树）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteTreeSelectMultiple url="/admin/sysOrg/tree" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteTreeTest() {
  const [value, setValue] = useState([]);
  return (
    <Section title="FieldRemoteTree — 扁平树多选（部门树）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteTree url="/admin/sysOrg/deptTree" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldRemoteTreeCascaderTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldRemoteTreeCascader — 远程树级联选择（组织树）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldRemoteTreeCascader url="/admin/sysOrg/tree" value={value} onChange={setValue} />
    </Section>
  );
}

function FieldSysOrgTreeTest() {
  const [value, setValue] = useState([]);
  return (
    <Section title="FieldSysOrgTree — 组织机构树（包装 FieldRemoteTree）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldSysOrgTree value={value} onChange={setValue} />
    </Section>
  );
}

function FieldSysOrgTreeSelectTest() {
  const [value, setValue] = useState();
  return (
    <Section title="FieldSysOrgTreeSelect — 组织机构树选择器（包装 FieldRemoteTreeSelect）">
      <div>选中值: {JSON.stringify(value)}</div>
      <FieldSysOrgTreeSelect value={value} onChange={setValue} />
    </Section>
  );
}

export default function () {
  return (
    <div style={{ padding: 24, maxWidth: 800 }}>
      <h1>RemoteSelect 系列组件测试</h1>
      <p style={{ color: '#888', marginBottom: 24 }}>
        所有组件已重构为继承 BaseRemoteSelect 基类。验证数据加载、搜索、选中功能正常。
      </p>
      <FieldRemoteSelectTest />
      <FieldRemoteSelectMultipleTest />
      <FieldRemoteSelectMultipleInlineTest />
      <FieldRemoteTreeSelectTest />
      <FieldRemoteTreeSelectMultipleTest />
      <FieldRemoteTreeTest />
      <FieldRemoteTreeCascaderTest />
      <FieldSysOrgTreeTest />
      <FieldSysOrgTreeSelectTest />
    </div>
  );
}

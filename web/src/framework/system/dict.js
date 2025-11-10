/***
 *
 * @param code
 */
import {Tag} from 'antd';
import React from 'react';
import {SysUtil} from "./sys";
import {StrUtil} from "@tmgg/tmgg-commons-lang";

// 根据字典类型code返回字典数据列表， code 支持 驼峰或下划线（都转为下划线比较）
export function dictList(code) {
  const dictTypeTree = SysUtil.getDictInfo()
  if (dictTypeTree === undefined) {
    return [];
  }

  let filtered = dictTypeTree.filter((item) => {
      return item.code === code || StrUtil.toUnderlineCase(item.code) === StrUtil.toUnderlineCase(code);
    }
  );

  if (filtered.length === 0) {
    console.log('字典' + code + '不存在,请刷新或在后台添加字典');
    return [];
  }
  const tree = filtered[0].children;
  if (tree === undefined) {
    return [];
  }
  return tree;
}

export function dictMap(typeCode) {
  const list = dictList(typeCode);

  if (list == null) {
    console.log('未找到数据字典, code=' + typeCode);
    return {};
  }
  const map = {};
  list.forEach((i) => {
    const code = i.code;

    // @ts-ignore
    map[code] = i;
  });

  return map;
}

export function dictValue(typeCode, code) {
  const item = dictData(typeCode, code);
  if (item) {
    return item.name;
  }
}

export function dictValueTag(typeCode, dataCode) {
  if (dataCode == null) {
    return '';
  }
  const data = dictData(typeCode, dataCode);
  if (data != null) {
    const {name, color} = data;

    if (color == null) {
      return name;
    }

    // js 方式创建 ，等同于 <Tag color={color}>{name}</Tag>
    return React.createElement(Tag, {color: color}, name);
  }
  return '';
}

export function dictData(typeCode, code) {
  if (code == null) {
    return null;
  }
  const map = dictMap(typeCode);
  // @ts-ignore
  let item = map[code];
  if (item == null) {
    // @ts-ignore
    item = map[code + ''];
  }
  return item;
}

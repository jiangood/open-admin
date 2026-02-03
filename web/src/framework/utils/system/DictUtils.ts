// 假设的类型定义，你需要确保与你的实际数据结构匹配
import {SysUtils} from "./SysUtils";
// 假设 SysUtil 和 StrUtil 的引入和类型
// 实际使用时需要确保路径和模块存在
import {Tag} from 'antd';
import React from 'react';
import {StringUtils} from "../StringUtils";

export interface DictItem {
    value: string | number;
    label: string;
    color?: string;
}


// 字典选项的格式
export interface DictOption {
    value: string | number;
    label: string;
}

/**
 * 字典相关的工具类
 */
export class DictUtils {

    /**
     * 根据字典类型code返回字典数据列表。
     * code 支持 驼峰或下划线（都转为下划线进行匹配）。
     * @param code 字典类型编码 (如 'userStatus', 'USER_STATUS')
     * @returns 对应的字典项列表，未找到返回空数组
     */
    public static dictList(code: string): DictItem[] {
        const map = SysUtils.getDictInfo();
        if (!map) {
            return [];
        }

        code = StringUtils.toUnderlineCase(code).toUpperCase();

        return map[code] || [];
    }

    /**
     * 将字典列表转换为 Ant Design Select/Options 格式
     * @param typeCode 字典类型编码
     * @returns 包含 value/label 的选项列表
     */
    public static dictOptions(typeCode: string): DictOption[] {
        const list: DictItem[] = DictUtils.dictList(typeCode);
        return list.map(i => {
            return {
                value: i.value,
                label: i.label,
            }
        });
    }


    /**
     * 根据字典类型和字典项code获取对应的名称(name)
     * @param typeCode 字典类型编码
     * @param code 字典项编码
     * @returns 对应的名称，未找到返回 undefined
     */
    public static dictLabel(typeCode: string, code: string | number): string | undefined {
        const items = DictUtils.dictList(typeCode);
        const item = items.find(i => i.value === code);
        return item ? item.label : undefined;
    }

    /**
     * 根据字典类型和字典项code获取对应的名称，并使用 Ant Design Tag 组件包装 (如果存在 color 属性)
     * @param typeCode 字典类型编码
     * @param code 字典项编码
     * @returns Antd Tag 元素或纯字符串名称或空字符串
     */
    public static dictTag(typeCode: string, code: string | number): React.ReactElement | string {
        if (typeCode == null || code == null) {
            return null;
        }
        const item = DictUtils.dictList(typeCode).find(i => i.value === code);

        if (item == null) {
            return null;
        }
        const {label, color} = item;

        if (color == null) {
            // 如果没有颜色，返回纯文本
            return label;
        }

        return React.createElement(Tag, {color: color}, label);
    }
}

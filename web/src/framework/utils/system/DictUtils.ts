// 假设的类型定义，你需要确保与你的实际数据结构匹配
export interface DictItem {
    code: string;
    text: string;
    name: string; // 用于 dictValue
    color?: string; // 用于 dictValueTag
    [key: string]: any; // 允许其他字段
}

// 假设 SysUtil.getDictInfo() 返回的结构
// Key 是字典类型代码 (大写下划线)，Value 是 DictItem 数组
export interface DictMap {
    [typeCode: string]: DictItem[] | undefined;
}

// 假设 SysUtil 和 StrUtil 的引入和类型
// 实际使用时需要确保路径和模块存在
import { Tag } from 'antd';
import React from 'react';
import { SysUtil } from "./sys"; // 假设 SysUtil 模块
import { StrUtil } from "../utils"; // 假设 StrUtil 模块

// 字典选项的格式
export interface DictOption {
    value: string;
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
        // 假设 SysUtil.getDictInfo() 返回 DictMap
        const map: DictMap | undefined = SysUtil.getDictInfo();
        if (map === undefined) {
            return [];
        }

        // 尝试使用原始大写Code
        const code1: string = code.toUpperCase();
        // 尝试使用下划线大写Code (例如 'userStatus' -> 'USER_STATUS')
        const code2: string = StrUtil.toUnderlineCase(code).toUpperCase();

        // 优先匹配 code1，然后 code2
        return map[code1] || map[code2] || [];
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
                value: i.code,
                label: i.text
            }
        });
    }

    /**
     * 将字典列表转换为以 code 为键的映射对象
     * @param typeCode 字典类型编码
     * @returns 以 code 为键，DictItem 为值的映射对象
     */
    public static dictMap(typeCode: string): Record<string, DictItem> {
        const list: DictItem[] = DictUtils.dictList(typeCode);

        if (list.length === 0) {
            console.log('未找到数据字典, code=' + typeCode);
            return {};
        }

        const map: Record<string, DictItem> = {};
        list.forEach((i: DictItem) => {
            // 使用 code 作为键
            map[i.code] = i;
        });

        return map;
    }

    /**
     * 根据字典类型和字典项code获取字典项数据
     * @param typeCode 字典类型编码
     * @param code 字典项编码 (可能为 number 或 string)
     * @returns 对应的字典项数据，未找到返回 null
     */
    public static dictData(typeCode: string, code: string | number | null | undefined): DictItem | null {
        if (code == null) {
            return null;
        }

        const map: Record<string, DictItem> = DictUtils.dictMap(typeCode);
        const codeStr: string = String(code); // 确保 code 转换为 string 键

        let item: DictItem | undefined = map[codeStr];

        // 原始代码中有一个冗余的 `map[code + '']` 检查，String(code) 已经做了相同的事情，但为了兼容性保留
        // if (item == null) {
        //     item = map[codeStr]; // 这里实际上就是上面的赋值，所以逻辑上只查一次即可
        // }

        return item || null;
    }

    /**
     * 根据字典类型和字典项code获取对应的名称(name)
     * @param typeCode 字典类型编码
     * @param code 字典项编码
     * @returns 对应的名称，未找到返回 undefined
     */
    public static dictValue(typeCode: string, code: string | number | null | undefined): string | undefined {
        const item: DictItem | null = DictUtils.dictData(typeCode, code);
        if (item) {
            return item.name;
        }
        return undefined;
    }

    /**
     * 根据字典类型和字典项code获取对应的名称，并使用 Ant Design Tag 组件包装 (如果存在 color 属性)
     * @param typeCode 字典类型编码
     * @param dataCode 字典项编码
     * @returns Antd Tag 元素或纯字符串名称或空字符串
     */
    public static dictValueTag(typeCode: string, dataCode: string | number | null | undefined): React.ReactElement | string {
        if (dataCode == null) {
            return '';
        }

        const data: DictItem | null = DictUtils.dictData(typeCode, dataCode);

        if (data != null) {
            const { name, color } = data;

            if (color == null) {
                // 如果没有颜色，返回纯文本
                return name;
            }

            // 使用 React.createElement 创建 Tag 组件
            return React.createElement(Tag, { color: color }, name);
        }

        return '';
    }
}

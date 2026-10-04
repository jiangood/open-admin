import {GlobalData} from '../GlobalData';
import {Tag} from 'antd';
import React from 'react';

export interface DictItem {
    code: string | number;
    label: string;
    color?: string;
    typeCode?: string;
}

export interface DictOption {
    value: string | number;
    label: string;
}

export class DictUtils {
    static dictList(code: string): DictItem[] {
        const info = GlobalData.getDictInfo();
        if (!info) return [];
        return info.filter(e => e.typeCode === code);
    }

    static dictOptions(typeCode: string): DictOption[] {
        const list = DictUtils.dictList(typeCode);
        return list.map(i => ({ value: i.code, label: i.label }));
    }

    static dictLabel(typeCode: string, code: string | number): string | undefined {
        const items = DictUtils.dictList(typeCode);
        const item = items.find(i => i.code === code);
        return item ? item.label : undefined;
    }

    static dictTag(typeCode: string, code: string | number): React.ReactElement | string | null {
        if (typeCode == null || code == null) return null;
        const item = DictUtils.dictList(typeCode).find(i => i.code === code);
        if (item == null) return null;
        const {label, color} = item;
        if (color == null) return label;
        return React.createElement(Tag, {color: color.toLowerCase()}, label);
    }
}

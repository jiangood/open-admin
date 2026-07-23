import React from 'react';
import {Tag} from 'antd';

export interface DictItem {
    code: string | number;
    label: string;
    color?: string;
}

export interface DictOption {
    value: string | number;
    label: string;
}

export class DictUtils {
    static dictList(code: string): DictItem[];
    static dictOptions(typeCode: string): DictOption[];
    static dictLabel(typeCode: string, code: string | number): string | undefined;
    static dictTag(typeCode: string, code: string | number): React.ReactElement | string | null;
}

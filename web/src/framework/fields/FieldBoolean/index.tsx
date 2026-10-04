/**
 * antd 自带的checkbox 未选择时不会返回false, 和form配合时还得设置 valuePropName
 *
 *
 * 本组件改成布尔值的输入
 *
 * 选中 true， 反选 false
 *
 * 注意：null 会转为false
 *
 */
import React from 'react';
import {Checkbox, Radio, Select, Switch} from 'antd';
import type {FieldProps} from '../types';

/** 布尔值的展示类型 */
export type FieldBooleanType = 'select' | 'radio' | 'checkbox' | 'switch';

export interface FieldBooleanProps extends FieldProps<boolean> {
    /** 展示类型，默认 select */
    type?: FieldBooleanType;
}

export class FieldBoolean extends React.Component<FieldBooleanProps> {

    static readonly defaultProps = {
        type: 'select'
    };

    render() {
        const {value, onChange, type} = this.props;

        const v = this.parseBoolean(value);

        switch (type) {
            case 'radio':
                return <Radio.Group value={v} onChange={e => onChange?.(e.target.value)}>
                    <Radio value={true}>是</Radio>
                    <Radio value={false}>否</Radio>
                </Radio.Group>;
            case 'checkbox':
                return <Checkbox
                    checked={v}
                    onChange={(e) => {
                        onChange?.(e.target.checked);
                    }}
                />;
            case 'select':
                return <Select options={[
                    {value: true, label: '是'},
                    {value: false, label: '否'},
                ]}
                               value={v}
                               onChange={value => onChange?.(value)}
                               style={{width: '100%'}}
                               allowClear={true}
                               placeholder={'请选择'}
                />;
            case 'switch':
                return <Switch
                    checked={v}
                    onChange={checked => onChange?.(checked)}
                />;
            default:
                return null;
        }

    }

    parseBoolean(v: unknown): boolean | undefined {
        if (v === null || v === undefined) {
            return undefined;
        }

        if (typeof v === 'boolean') {
            return v;
        }
        return v === 1 || v === 'true' || v === 'Y';

    }
}
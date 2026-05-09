import React from "react";
import {Select} from "antd";
import {DictUtils} from "../../utils";
import type {SelectProps} from "antd/es/select";
import type {FieldProps} from '../types';

interface FieldDictSelectProps extends Omit<SelectProps, 'options' | 'children' | 'mode' | 'value' | 'onChange'>, FieldProps<any> {
    typeCode: string;
}

export class FieldDictSelect extends React.Component<FieldDictSelectProps> {
    render() {
        const {value, typeCode} = this.props;
        const options = DictUtils.dictOptions(typeCode);
        const strValue = value == null ? null : String(value);

        return <Select value={strValue}
                       onChange={this.props.onChange}
                       style={{width: '100%', minWidth: 200}}
                       options={options}
        />;
    }
}

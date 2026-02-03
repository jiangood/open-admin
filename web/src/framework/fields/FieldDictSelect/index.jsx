import React from "react";
import {Select} from "antd";
import {DictUtils} from "../../utils";

export function FieldDictSelect(props) {
    const {value, onChange, typeCode, ...rest} = props;
    const options = DictUtils.dictOptions(typeCode);


    return <Select value={String(value)}
                   onChange={onChange}
                   style={{width: '100%', minWidth: 200}}
                   options={options}
                   {...rest}>

    </Select>;
}
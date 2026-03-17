import React from "react";
import {Select} from "antd";
import {DictUtils} from "../../utils";

export class FieldDictSelect extends React.Component {
    render() {
        const {value,  typeCode} = this.props;
        const options = DictUtils.dictOptions(typeCode);
        const strValue = value == null ? null : String(value);

        return <Select value={strValue}
                       onChange={this.props.onChange}
                       style={{width: '100%', minWidth: 200}}
                       options={options}
                   />
    }
}
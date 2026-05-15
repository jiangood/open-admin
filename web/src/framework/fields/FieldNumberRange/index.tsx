import React from "react";
import {InputNumber} from "antd";
import {StringUtils} from "../../utils";

const SP = StringUtils.ISO_SPLITTER;

export class FieldNumberRange extends React.Component {
    onChangeA = (a) => {
        const {b} = this.parse(this.props.value);
        this.props.onChange && this.props.onChange(this.merge(a, b));
    };

    onChangeB = (b) => {
        const {a} = this.parse(this.props.value);
        this.props.onChange && this.props.onChange(this.merge(a, b));
    };

    merge(a, b) {
        if (a == null) {
            a = '';
        }
        if (b == null) {
            b = '';
        }
        return a + SP + b;
    }

    parse(v) {
        if (v == null) {
            return {a: null, b: null};
        }
        const arr = v.split(SP);
        return {a: arr[0] ? Number(arr[0]) : null, b: arr[1] ? Number(arr[1]) : null};
    }

    componentDidMount() {
        const {value, defaultValue} = this.props;
        if (value == null && defaultValue) {
            this.props.onChange && this.props.onChange(defaultValue);
        }
    }

    render() {
        let {value, defaultValue} = this.props;
        if (value == null) {
            value = defaultValue;
        }
        const {a, b} = this.parse(value);

        return <div style={{display: 'flex', alignItems: 'center'}}>
            <InputNumber value={a} onChange={this.onChangeA}/> - <InputNumber value={b} onChange={this.onChangeB}/>
        </div>;
    }

}
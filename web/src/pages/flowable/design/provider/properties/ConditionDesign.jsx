import {Button, Input, InputNumber, Modal, Select} from "antd";
import React, {Component} from "react";
import {FieldBoolean, FieldTable, HttpUtils, StringUtils, ThemeUtils} from "../../../../../framework";

const metaInfo = {
    STRING: {
        ops: {
            '==': '等于',
            '!=': '不等于',
            contains: '包含',
            '!contains': '不包含',
            startWith: '开头等于',
            endWith: '结尾等于',
        },
        render() {
            return <Input/>;
        },
    },
    NUMBER: {
        ops: {
            '==': '等于',
            '!=': '不等于',
            '>': '大于',
            '<': '小于',
            '>=': '大于等于',
            '<=': '小于等于',
            between: '介于',
        },
        render(op) {
            if (op !== 'between') return <InputNumber/>;

            return (
                <Input.Group>
                    <InputNumber placeholder="最小值"></InputNumber>
                    <InputNumber placeholder="最大值"></InputNumber>
                </Input.Group>
            );
        },
    },
    BOOLEAN: {
        ops: {
            '==': '等于',
        },
        render() {
            return <FieldBoolean/>;
        },
    },
};

export class ConditionDesignButton extends Component {

    state = {
        open: false,
        varList: [],
        varOptions: []
    }

    componentDidMount() {
        const {processId} = this.props;
        console.log('流程id', processId)

        HttpUtils.get('admin/flowable/model/varList', {code: processId}).then(rs => {

            const options = rs.map(r => {
                return {
                    label: r.label,
                    value: r.name
                }
            })
            this.setState({varList: rs, varOptions: options})
        })
    }

    onChange = arr => {
        const str = this.convertArrToStr(arr)
        this.props.setValue(str, this.props.element, this.props.modeling, this.props.bpmnFactory)
    };

    columns = [
        {
            dataIndex: 'key', title: '变量名称',
            render: () => {
                return <Select options={this.state.varOptions} style={{width: 200}}></Select>
            }
        },
        {
            dataIndex: 'op', title: '操作符', render: (v, record) => {
                let options = []
                let {varList} = this.state;
                let varItem = varList.find(t => t.name === record.key)

                if (varItem) {
                    const {valueType} = varItem;
                    const ops = metaInfo[valueType].ops;
                    options = Object.keys(ops).map(key => {
                        return {
                            label: ops[key],
                            value: key
                        }
                    })
                }

                return <Select options={options} style={{width: 100}}></Select>
            }
        },
        {dataIndex: 'value', title: '值'},
    ];

    render() {
        let value = this.props.getValue(this.props.element);
        let arrValue = this.convertStrToArr(value);

        return <div style={{display: 'flex', justifyContent: 'right', padding: 8}}>
            <Button type='primary' size='small'
                    styles={{
                        root: {backgroundColor: ThemeUtils.getColor('primary-color')}
                    }}
                    onClick={() => this.setState({open: true})}
            >条件编辑器</Button>


            <Modal title='条件编辑器 (复杂表达式暂不支持)' open={this.state.open}
                   onCancel={() => this.setState({open: false})}
                   footer={null}
                   mask={{blur: false}}
                   destroyOnHidden
            >
                <FieldTable columns={this.columns}
                            value={arrValue}
                            onChange={this.onChange}
                ></FieldTable>
            </Modal>

        </div>
    }

    convertStrToArr(value) {

        let arrValue = []
        if (value) {


            value = StringUtils.removePrefix(value, "${")
            value = StringUtils.removeSuffix(value, "}")
            const strArr = StringUtils.split(value, '&&');

            const regex = /(\w+)([<>=!]+)(\w+)/;

            strArr.forEach(expression => {
                const parts = expression.match(regex);
                // parts[0] 是整个匹配的字符串
                // parts[1] 是第一个捕获组 (左操作数)
                // parts[2] 是第二个捕获组 (操作符)
                // parts[3] 是第三个捕获组 (右操作数)
                arrValue.push({
                    key: parts[1],
                    op: parts[2],
                    value: parts[3],
                })
            })
        }
        return arrValue;
    }

    convertArrToStr = arrValue => {
        const str = arrValue.map(i => {
            const {key, op, value} = i;
            return `${key}${op}${value}`
        }).join('&&')

        return "${" + str + "}"
    };
}



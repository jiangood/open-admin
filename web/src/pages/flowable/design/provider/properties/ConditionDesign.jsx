import {Button, Input, InputNumber, Modal, Select} from "antd";
import {Component} from "react";
import {FieldBoolean, FieldTable, HttpUtils, ObjectUtils, StringUtils, ThemeUtils} from "../../../../../framework";
import {ConditionExpressionUtils} from "./ConditionExpressionUtils";


// 字符串的双引号
const QUOTE = '"';


const OPERATOR_DEFINITIONS = [
    {
        type: 'STRING',
        label: '等于',
        key: '==',
        component: Input,
    },
    {
        type: 'STRING',
        label: '不等于',
        key: '!=',
        component: Input,
    },
    {
        type: 'STRING',
        label: '包含',
        key: '.contains',
        component: Input,
    },
    {
        type: 'STRING',
        label: '开头等于',
        key: '.startWith',
        component: Input,
    },
    {
        type: 'STRING',
        label: '结尾等于',
        key: '.endWith',
        component: Input,
    },

    // =================== 数字 ================

    {
        type: 'NUMBER',
        label: '等于',
        key: '==',
        component: Input,
    },
    {
        type: 'NUMBER',
        label: '不等于',
        key: '!=',
        component: Input,
    },

    {
        type: 'NUMBER',
        label: '大于',
        key: '>',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '小于',
        key: '<',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '大于等于',
        key: '>=',
        component: InputNumber,
    },
    {
        type: 'NUMBER',
        label: '小于等于',
        key: '<=',
        component: InputNumber,
    },


    // ===================== 布尔值 =======================

    {
        type: 'BOOLEAN',
        label: '等于',
        key: '==',
        component: FieldBoolean,
    },

]


function encode(data) {
    let {left, op, right} = data;
    if (left == null || op == null || right == null) {
        return null
    }

    const isFun = op.startsWith('.')
    if (isFun) {
        return left +   op + '("' + right + '")';
    }
    const isStr = right.startsWith('"')
    if (isStr) {
        right = '"' + right + '"';
    }

    return left + op + right;
}

function decode(expression) {
    const isFun = ConditionExpressionUtils.isFunction(expression);
    if (isFun) {
        return ConditionExpressionUtils.parseStrFunction(expression)
    }

    return ConditionExpressionUtils.parse(expression)
}


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

    getOptionsByItem = (record) => {
        let options = []
        let {varList} = this.state;
        let varItem = varList.find(t => t.name === record.left)

        if (varItem) {
            const {valueType} = varItem;
            const os = OPERATOR_DEFINITIONS.filter(o => o.type === valueType)
            for (let o of os) {
                options.push({
                    label: o.label,
                    value: o.key
                })
            }
        }

        return options;
    }

    columns = [
        {
            dataIndex: 'left', title: '变量名称',
            render: () => {
                return <Select options={this.state.varOptions} style={{width: 200}}></Select>
            }
        },
        {
            dataIndex: 'op', title: '操作符',
            render: (v, record) => {
                const options = this.getOptionsByItem(record)

                return <Select options={options} style={{width: 100}}></Select>
            }
        },
        {dataIndex: 'right', title: '值'},
    ];

    render() {
        let value = this.props.getValue(this.props.element);
        let arrValue = this.convertStrToArr(value);

        return <div style={{display: 'flex', justifyContent: 'right', padding: 8}}>
            <Button type='primary'
                    size='small'
                    styles={{
                        root: {
                            backgroundColor: ThemeUtils.getColor('primary-color')
                        }
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
        if (value) {
            value = StringUtils.removePrefixAndSuffix(value, "${", "}")
            const strArr = StringUtils.split(value, '&&');
            return strArr.map(decode).filter(t => t != null)
        }
        return [];
    }


    convertArrToStr = arrValue => {
        const str = arrValue.map(encode).join('&&')

        return "${" + str + "}"
    };


}



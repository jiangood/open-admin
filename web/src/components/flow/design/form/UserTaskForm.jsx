import React from 'react';
import {Card, Divider, Form, Input, Select} from 'antd';
import BpmnUtils from '../../BpmnUtils';
import {HttpUtil} from "@tmgg/tmgg-base";

const PREFIX = 'flowable:';


export default class extends React.Component {
    state = {
        assignmentType: null,
        assignmentTypeList: [],
        assignmentObjectList: [],

        assignmentTypeMap: {},

        initData: {},

        loading: true,

        formKeyOptions:[]
    };

    formRef = React.createRef();

    destroyed = false;

    constructor(props) {
        super(props);

        const data = {};
        data.assignmentType = this.props.bo.get('flowable:assignmentType');
        data.assignmentObject = this.props.bo.get('flowable:assignmentObject');
        data['flowable:assignee'] = this.props.bo.get('flowable:assignee');
        data['flowable:formKey'] = this.props.bo.get('flowable:formKey');
        this.state.initData = data;
        this.state.assignmentType = data.assignmentType

        if(this.props.model.formKeyList){
            this.state.formKeyOptions = props.model.formKeyList.map(k=>{
                return {
                    label:k.label,
                    value:k.value
                }
            })
        }

    }

    componentDidMount() {
        const {initData} = this.state;
        HttpUtil.get('/flowable/model/assignmentTypeList').then(assignmentTypeList => {
            if (this.destroyed) {
                return;
            }

            const assignmentTypeMap = {};

            assignmentTypeList.forEach((a) => {
                assignmentTypeMap[a.code] = a;
            });

            this.setState({assignmentTypeList, assignmentTypeMap});

            // 如果已指定分配类型
            if (initData.assignmentType) {
                const assignmentTypeInfo = assignmentTypeMap[initData.assignmentType];

                if (assignmentTypeInfo) {
                    this.setState({assignmentTypeInfo});
                    this.loadAssignmentObjectTree(initData.assignmentType);
                }

                // 如果已指定分配对象
                if (initData.assignmentObject != null && assignmentTypeInfo != null) {
                    if (assignmentTypeInfo.multiple && !Array.isArray(initData.assignmentObject)) {
                        initData.assignmentObject = initData.assignmentObject.split(',');
                    }
                }
            }
            this.setState({loading: false});
            console.log('initData', initData);
        });
    }

    loadAssignmentObjectTree = (assignmentTypeCode) => {
        const url = '/flowable/model/assignmentObjectTree?code=' + assignmentTypeCode;
        HttpUtil.get(url).then((rs) => {
            this.setState({assignmentObjectList: rs});
        });
    };


    onValuesChange = (changed, values) => {
        for (let k in changed) {
            let v = changed[k];
            let fullKey = k.startsWith(PREFIX) ? k : PREFIX + k;
            if (v == null) {
                BpmnUtils.removeProperty(this.props.bo, fullKey);
            } else {
                this.props.bo.set(fullKey, v);
            }

            // 选择用户或角色等具体
            switch (k) {
                case 'assignmentObject':
                    this.onAssignmentObjectChange(k, v, values)
                    break;
                case 'assignmentType':
                    this.setState({assignmentType: v})
                    this.onAssignmentTypeChange(v)
                    break;
            }

        }
    };

    onAssignmentTypeChange = (type) => {
        let exp = type === 'assigneeExpression';
        if (type != null && !exp) {
            this.loadAssignmentObjectTree(type);
        }

        if (!exp) {
            const assignmentTypeInfo = this.state.assignmentTypeMap[type];
            if (assignmentTypeInfo.multiple) {
                this.formRef.current.setFieldsValue({assignmentObject: []});
            } else {
                this.formRef.current.setFieldsValue({assignmentObject: null});
            }
        }

        BpmnUtils.removeProperty(this.props.bo, PREFIX + 'assignmentObject');
        BpmnUtils.removeProperty(this.props.bo, PREFIX + 'assignee');
        BpmnUtils.removeProperty(this.props.bo, PREFIX + 'candidateGroups');
        BpmnUtils.removeProperty(this.props.bo, PREFIX + 'candidateUsers');
    }


    onAssignmentObjectChange = (k, v, values) => {
        // 设置自定义属性
        const typeInfo = this.state.assignmentTypeMap[values.assignmentType];
        const xmlKey = typeInfo.xmlAttribute;
        this.props.bo.set(PREFIX + xmlKey, v);
    }


    componentWillUnmount() {
        this.destroyed = true;
    }

    render() {
        if (this.state.loading) {
            return <Card loading={true} variant={"borderless"}></Card>;
        }

        const {assignmentTypeList, assignmentObjectList} = this.state;
        const assignmentType = this.state.assignmentType;
        const assignmentTypeInfo = this.state.assignmentTypeMap[assignmentType];
        const assignmentObjectOptions = assignmentObjectList.map((t) => ({
                key: t.value,
                label: t.label,
                value: t.value
            }
        ))

        return <>
            <Form
                ref={this.formRef}
                onValuesChange={this.onValuesChange}
                initialValues={this.state.initData}
                layout={'vertical'}
            >
                <Form.Item label="人员分配方式" name="assignmentType">
                    <Select allowClear={true}>
                        {assignmentTypeList.map((t) => <Select.Option key={t.code} value={t.code}>
                                {t.label}
                            </Select.Option>)}
                        <Select.Option key='assigneeExpression'
                                       value='assigneeExpression'>分配给单个用户（表达式）</Select.Option>
                    </Select>

                </Form.Item>

                {assignmentTypeInfo && <Form.Item label="分配目标" name="assignmentObject">
                        <Select
                            mode={assignmentTypeInfo.multiple ? 'multiple' : false}
                            allowClear={true}
                            showSearch={true}
                            filterOption={(input, option) => option.label.includes(input)}
                            options={assignmentObjectOptions}
                        >
                        </Select>
                    </Form.Item>}
                {assignmentType === 'assigneeExpression' && <Form.Item label='表达式' name='flowable:assignee'>
                    <Input/>
                </Form.Item>}



                <Divider />

                <Form.Item label='指定表单' name='flowable:formKey'>
                    <Select options={this.state.formKeyOptions} placeholder='请选择表单' allowClear/>
                </Form.Item>

            </Form>
        </>;
    }
}

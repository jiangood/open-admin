import React from "react";
import {Button, Card, Divider, Form, Space, Splitter} from "antd";
import {
    FieldBoolean, FieldDate,
    FieldEditor,
    FieldRemoteSelect,
    FieldRemoteSelectMultiple,
    FieldRemoteTree,
    FieldRemoteTreeCascader, FieldRemoteTreeSelect, FieldRemoteTreeSelectMultiple
} from "../framework";

export default class extends React.Component {


    state = {
        formValues: {}
    }


    componentDidMount() {
        const json = localStorage.getItem("_test")
        const formValues = json ? JSON.parse(json) : {}
        this.formRef.current.setFieldsValue(formValues)
        this.setState({formValues})
    }

    formRef = React.createRef()


    render() {

        return <>
            <Card title='表单组件'>
                {JSON.stringify(this.state.formValues)}
                <Divider></Divider>
                <Form
                    ref={this.formRef}
                    onValuesChange={(changedValues, allValues) => {
                        this.setState({formValues: allValues})
                    }}
                    onReset={() => {
                        this.setState({formValues: {}})
                    }}

                    onFinish={values => {
                        localStorage.setItem("_test", JSON.stringify(values))
                    }}

                    labelAlign={'left'}
                    labelCol={{flex: '400px',}}
                    layout='horizontal'
                >
                    <Form.Item label='FieldDate YEAR_MONTH_DAY_HOUR_MINUTE_SECOND' name='date'>
                        <FieldDate type='YEAR_MONTH_DAY_HOUR_MINUTE_SECOND'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate YEAR_MONTH_DAY_HOUR_MINUTE' name='date'>
                        <FieldDate type='YEAR_MONTH_DAY_HOUR_MINUTE'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate 日期' name='date'>
                        <FieldDate ></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate YEAR_QUARTER 季度' name='date'>
                        <FieldDate type='YEAR_QUARTER'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate YEAR_MONTH' name='date'>
                        <FieldDate type='YEAR_MONTH'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate YEAR' name='date'>
                        <FieldDate type='YEAR'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate HOUR_MINUTE' name='date'>
                        <FieldDate type='HOUR_MINUTE'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldDate HOUR_MINUTE_SECOND' name='date'>
                        <FieldDate type='HOUR_MINUTE_SECOND'></FieldDate>
                    </Form.Item>
                    <Form.Item label='FieldBoolean 布尔 (switch)' name='boolean'>
                        <FieldBoolean type='switch'></FieldBoolean>
                    </Form.Item>
                    <Form.Item label='FieldBoolean 布尔 (checkbox)' name='boolean'>
                        <FieldBoolean type='checkbox'></FieldBoolean>
                    </Form.Item>
                    <Form.Item label='FieldBoolean 布尔 (radio)' name='boolean'>
                        <FieldBoolean type='radio'></FieldBoolean>
                    </Form.Item>
                    <Form.Item label='FieldBoolean 布尔 (选择框)' name='boolean'>
                        <FieldBoolean type='select'></FieldBoolean>
                    </Form.Item>
                    <Form.Item label='FieldRemoteTreeSelectMultiple 远程树选择(多选)' name='users'>
                        <FieldRemoteTreeSelectMultiple url='admin/sysUser/tree'/>
                    </Form.Item>
                    <Form.Item label='远程树选择' name='user'>
                        <FieldRemoteTreeSelect url='admin/sysUser/tree'/>
                    </Form.Item>
                    <Form.Item label='远程树（多选）' name='users'>
                        <FieldRemoteTree url='admin/sysUser/tree'/>
                    </Form.Item>
                    <Form.Item label='远程树级联选择' name='user'>
                        <FieldRemoteTreeCascader url='admin/sysUser/tree'/>
                    </Form.Item>

                    <Form.Item label='单选' name='user'>
                        <FieldRemoteSelect url='admin/sysUser/options'/>
                    </Form.Item>
                    <Form.Item label='多选' name='users'>
                        <FieldRemoteSelectMultiple url='admin/sysUser/options'/>
                    </Form.Item>
                    <Form.Item label='富文本' name='富文本'>
                        <FieldEditor height={100}/>
                    </Form.Item>

                    <Space>
                        <Button type='primary' htmlType='submit'>保存</Button>
                        <Button htmlType='reset'>重置</Button>
                    </Space>
                </Form>


            </Card>
        </>
    }
}

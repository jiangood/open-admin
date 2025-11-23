import React from "react";
import {Button, Card, Divider, Form, Space, Splitter} from "antd";
import {FieldEditor, FieldRemoteSelect, FieldRemoteSelectMultiple, FieldRemoteTreeCascader} from "../framework";

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

                            layout='vertical'
                        >
                            <Form.Item label='单选' name='单选值'>
                                <FieldRemoteSelect url='admin/sysUser/options'/>
                            </Form.Item>
                            <Form.Item label='多选' name='多选值'>
                                <FieldRemoteSelectMultiple url='admin/sysUser/options'/>
                            </Form.Item>
                            <Form.Item label='富文本' name='富文本'>
                                <FieldEditor height={100} />
                            </Form.Item>
                            <Form.Item label='级联数选择' name='级联数选择'>
                                <FieldRemoteTreeCascader url='admin/sysUser/tree' />
                            </Form.Item>

                            <Space>
                                <Button type='primary' htmlType='submit'>保存</Button>
                                <Button htmlType='reset'>重置</Button>
                            </Space>
                        </Form>
                        <Divider>表单数据</Divider>
                        {JSON.stringify(this.state.formValues)}



            </Card>
        </>
    }
}

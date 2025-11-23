import React from "react";
import {Button, Card, Divider, Form, Space, Splitter} from "antd";
import {
    FieldEditor,
    FieldRemoteSelect,
    FieldRemoteSelectMultiple,
    FieldRemoteTree,
    FieldRemoteTreeCascader, FieldRemoteTreeSelect
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

                            labelCol={{flex:'200px'}}
                            layout='horizontal'
                        >
                            <Form.Item label='远程树选择' name='远程树选择'>
                                <FieldRemoteTreeSelect url='admin/sysUser/tree' />
                            </Form.Item>
                            <Form.Item label='远程树' name='远程树'>
                                <FieldRemoteTree url='admin/sysUser/tree' />
                            </Form.Item>
                            <Form.Item label='远程树级联选择' name='级联数选择'>
                                <FieldRemoteTreeCascader url='admin/sysUser/tree' />
                            </Form.Item>

                            <Form.Item label='单选' name='单选值'>
                                <FieldRemoteSelect url='admin/sysUser/options'/>
                            </Form.Item>
                            <Form.Item label='多选' name='多选值'>
                                <FieldRemoteSelectMultiple url='admin/sysUser/options'/>
                            </Form.Item>
                            <Form.Item label='富文本' name='富文本'>
                                <FieldEditor height={100} />
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

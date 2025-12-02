import React from "react";
import {Button, Card, Form, Input, message, Radio, Spin, Splitter,} from "antd";
import {history} from "umi";
import {HttpUtils, Page} from "../../../framework";
import InstanceInfo from "../InstanceInfo";

export default class extends React.Component {

    state = {
        submitLoading: false,
        taskId: null,
        instanceId: null,
        formKey: null,

        taskInfo: null
    }



    externalFormRef = React.createRef()

    componentDidMount() {
        const {taskId, instanceId, formKey} = PageUtils.currentParams()
        this.setState({taskId, instanceId, formKey})

        HttpUtils.get('admin/flowable/my/taskInfo', {id: taskId}).then(rs=>{
            console.log('任务信息',rs)
            this.setState({taskInfo:rs})
        })

    }

    handleTask = async value => {
        this.setState({submitLoading: true});
        try {
            if (value.result === 'APPROVE') {
                const fn = this.externalFormRef.current?.submit
                if (fn) {
                    await fn()
                }
            }

            value.taskId = this.state.taskId
            await HttpUtils.post("admin//flowable/my/handleTask", value)
            history.replace('/flowable/task')
        } catch (error) {
            message.error(error)
        } finally {
            this.setState({submitLoading: false})
        }

    }

    render() {
        const {submitLoading,taskInfo} = this.state
        const instanceId = this.state.instanceId
        if (!instanceId || !taskInfo) {
            return <Spin/>
        }
        return <Page padding>

            <Splitter>
                <Splitter.Panel>
                    <InstanceInfo id={instanceId} formKey={this.state.formKey} externalFormRef={this.externalFormRef} taskInfo={taskInfo}/>
                </Splitter.Panel>
                <Splitter.Panel defaultSize={400}>
                    <Card title='审批意见'>
                        <Form
                            layout='vertical'
                            onFinish={this.handleTask}
                            disabled={submitLoading}
                        >
                            <Form.Item label='审批结果' name='result' rules={[{required: true, message: '请选择'}]}
                                       initialValue={'APPROVE'}>
                                <Radio.Group>
                                    <Radio value='APPROVE'>同意</Radio>
                                    <Radio value='REJECT'>不同意</Radio>
                                </Radio.Group>
                            </Form.Item>
                            <Form.Item label='审批意见' name='comment'
                                       rules={[{required: true, message: '请输入审批意见'}]}>
                                <Input.TextArea/>
                            </Form.Item>
                            <div>
                                <Button type='primary' htmlType='submit' loading={submitLoading}
                                        size={"middle"}>提&nbsp;交</Button>
                            </div>
                        </Form>
                    </Card>
                </Splitter.Panel>

            </Splitter>


        </Page>


    }
}

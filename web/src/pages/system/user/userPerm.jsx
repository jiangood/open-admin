import {Form, Modal, message} from 'antd';
import React from 'react';
import {FieldDictSelect, FieldRemoteSelect, FieldSysOrgTree, HttpClient} from "../../../framework";


export default class UserPerm extends React.Component {


    state = {
        visible: false,

        confirmLoading: false,

        formValues: {
            dataPermType: null
        },
    }
    formRef = React.createRef() // NOSONAR: React ref，内部通过 this.formRef 使用

    show(item) { // NOSONAR: ref 暴露给父组件调用的公共 API
        this.setState({visible: true})

        HttpClient.get('admin/sysUser/get-perm-info', {id: item.id}, rs => {
            this.setState({formValues: rs})
            this.formRef.current.setFieldsValue(rs)
        })
    }

    handleSave = (values) => {
        values.grantOrgIdList = this.state.checked

        this.setState({
            confirmLoading: true
        })


        HttpClient.post('admin/sysUser/grant-perm', values, null, () => {
            this.setState({
                visible: false,
                confirmLoading: false
            })
            this.props.onOk()
        }, e => {
            message.error(HttpClient.errToMsg(e))
            this.setState({
                confirmLoading: false
            })
        })
    }

    render() {
        const {visible, confirmLoading} = this.state

        return <Modal
            title="授权"
            destroyOnHidden
            width={600}
            open={visible}
            confirmLoading={confirmLoading}
            onCancel={() => this.setState({visible: false})}
            onOk={() => this.formRef.current.submit()}
        >

            <Form ref={this.formRef}
                  onFinish={this.handleSave}
                  onValuesChange={(change, values) => {
                      this.setState({formValues: values})
                  }}
                  labelCol={{flex: '100px'}}
            >
                <Form.Item name='id' noStyle></Form.Item>
                <Form.Item label='角色' name='roleIds' rules={[{required: true}]}>
                    <FieldRemoteSelect url='admin/sysRole/options' multiple />
                </Form.Item>
                <Form.Item label='数据权限' name='dataPermType' rules={[{required: true}]}>
                    <FieldDictSelect typeCode='dataPermType'/>
                </Form.Item>


                {this.state.formValues.dataPermType === 'CUSTOM' &&
                    <Form.Item label='组织机构' name='orgIds'>
                        <FieldSysOrgTree/>
                    </Form.Item>
                }


            </Form>


        </Modal>
    }


}

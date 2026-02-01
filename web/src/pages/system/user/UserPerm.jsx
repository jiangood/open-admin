import {Form, Modal} from 'antd';
import React from 'react';
import {FieldDictSelect, FieldRemoteSelectMultiple, FieldSysOrgTree, HttpUtils} from "../../../framework";


export default class UserPerm extends React.Component {


    state = {
        visible: false,

        confirmLoading: false,

        formValues: {
            dataPermType: null
        },
    }
    formRef = React.createRef()

    show(item) {
        this.setState({visible: true})

        HttpUtils.get('admin/sysUser/getPermInfo', {id: item.id}).then(rs => {
            this.setState({formValues: rs})
            this.formRef.current.setFieldsValue(rs)
        })
    }

    handleSave = (values) => {
        values.grantOrgIdList = this.state.checked

        this.setState({
            confirmLoading: true
        })


        HttpUtils.post('admin/sysUser/grantPerm', values).then(rs => {
            this.setState({
                visible: false,
                confirmLoading: false
            })
            this.props.onOk()
        }).finally(() => {
            this.setState({
                confirmLoading: false
            })

        })
    }

    render() {
        let {visible, confirmLoading} = this.state

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
                    <FieldRemoteSelectMultiple url='admin/sysRole/options'/>
                </Form.Item>
                <Form.Item label='数据权限' name='dataPermType' rules={[{required: true}]}>
                    <FieldDictSelect typeCode='dataPermType'/>
                </Form.Item>


                {this.state.formValues.dataPermType === 'CUSTOM' && <>
                    <Form.Item label='组织机构' name='orgIds'>
                        <FieldSysOrgTree/>
                    </Form.Item>
                </>}


            </Form>


        </Modal>
    }


}

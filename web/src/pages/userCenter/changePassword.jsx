import React from "react";
import {Button, Form, Input, Modal} from "antd";
import {HttpUtils, history} from "../../framework";

export default class ChangePassword extends React.Component {

    formRef = React.createRef();

    state = {
        successModalOpen: false,
    };

    onFinish = (values) => {
        HttpUtils.post('admin/userCenter/update-pwd', values).then(() => {
            this.setState({successModalOpen: true});
        })
    }

    validator = (rule, value) => {
        return new Promise((resolve, reject) => {
            HttpUtils.get("admin/sysUser/pwd-strength", {password: value}, {showError: false}).then(response => {
                const rs = response.data
                if (!rs.success) {
                    reject(rs.message)
                }
                resolve()
            })
        })

    }

    render() {
        return <>
            <Modal open={this.props.open} title="修改密码" okText="确定" cancelText="取消"
                   onCancel={this.props.onClose}
                   onOk={() => this.formRef?.submit()}>
                <Form ref={this.formRef} onFinish={this.onFinish} style={{maxWidth: 400}}>
                    <Form.Item name='newPassword'
                               label='新密码'
                               extra={'请输入字母、数字、特殊字符'}
                               rules={[
                                   {required: true},
                                   {
                                       validator: this.validator
                                   }
                               ]}
                    >
                        <Input.Password></Input.Password>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal open={this.state.successModalOpen} title="提示" okText="确定"
                   onCancel={() => this.setState({successModalOpen: false})}
                   onOk={() => {
                       this.setState({successModalOpen: false});
                       history.push('/public/login');
                   }}>
                修改密码成功
            </Modal>
        </>
    }
}

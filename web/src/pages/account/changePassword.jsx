import React from "react";
import {Button, Form, Input, Modal} from "antd";
import {HttpUtils, history} from "../../framework";

export default class extends React.Component {

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
        return <div>
            <Modal open={this.state.successModalOpen} title="提示" okText="确定"
                   onCancel={() => this.setState({successModalOpen: false})}
                   onOk={() => {
                       this.setState({successModalOpen: false});
                       history.push('/public/login');
                   }}>
                修改密码成功
            </Modal>

            <Form onFinish={this.onFinish} style={{maxWidth: 400}}>

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

                <Form.Item wrapperCol={{offset: 5}} style={{marginTop: 40}}>
                    <Button type="primary" htmlType="submit">
                        确定
                    </Button>
                </Form.Item>
            </Form>

        </div>
    }
}

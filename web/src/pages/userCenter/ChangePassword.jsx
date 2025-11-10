import React from "react";
import {Button, Form, Input, Modal} from "antd";


import {history} from 'umi'
import {HttpUtil, SysUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {


    onFinish = (values) => {
        HttpUtil.post('userCenter/updatePwd', values).then(() => {
            Modal.success({
                title: '提示',
                content: '修改密码成功',
                onOk: () => {
                    SysUtil.setToken(null)
                    history.push('/login')
                }
            })
        })
    }

    validator = (rule, value) => {
        return new Promise((resolve, reject) => {
            HttpUtil.create().get("/sysUser/pwdStrength", {password: value}).then(response => {
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

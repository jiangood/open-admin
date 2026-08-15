import React from "react";
import {Button, Form, Input, message} from "antd";
import {HttpClient, history} from "../../framework";

export default class ForceUpdatePwdPage extends React.Component {

    onFinish = (values) => {
        HttpClient.post('admin/userCenter/update-pwd', values, null, () => {
            message.success('修改密码成功，请重新登录');
            history.push('/public/login');
        })
    }

    validator = (rule, value) => new Promise((resolve, reject) => {
        HttpClient.get("admin/sysUser/pwd-strength", {password: value}, resolve, (e) => reject(e.message))
    })

    render() {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                background: '#f0f2f5'
            }}>
                <div style={{
                    background: '#fff',
                    padding: '40px',
                    borderRadius: '8px',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                    maxWidth: 400,
                    width: '100%'
                }}>
                    <h2 style={{textAlign: 'center', marginBottom: 24}}>修改密码</h2>
                    <p style={{textAlign: 'center', color: '#999', marginBottom: 24}}>
                        首次登录或密码已被重置，请修改密码后重新登录
                    </p>
                    <Form onFinish={this.onFinish}>
                        <Form.Item name='newPassword'
                                   label='新密码'
                                   extra={'请输入字母、数字、特殊字符'}
                                   rules={[
                                       {required: true, message: '请输入新密码'},
                                       {pattern: /^[\x20-\x7E]{1,64}$/, message: '密码仅支持英文、数字与常见符号，长度不超过64位'},
                                       {validator: this.validator}
                                   ]}
                        >
                            <Input.Password maxLength={64}/>
                        </Form.Item>
                        <Form.Item style={{marginTop: 40}}>
                            <Button type="primary" htmlType="submit" block size='large'>
                                确定
                            </Button>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        )
    }
}

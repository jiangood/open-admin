import React from "react";
import {Button, Form, Input, message} from "antd";
import {history} from 'umi'
import {HttpUtils, PageUtils, GlobalData} from "../framework";

export default class extends React.Component {

    state = {
        checking: true
    }

    async componentDidMount() {
        try {
            const rs = await HttpUtils.get('/admin/public/user-info', null, {showError: false});
            if (!rs.login) {
                PageUtils.redirectToLogin();
                return;
            }
            GlobalData.setLoginInfo(rs.loginInfo);
            this.setState({checking: false});
        } catch (e) {
            PageUtils.redirectToLogin();
        }
    }

    onFinish = (values) => {
        HttpUtils.post('admin/userCenter/update-pwd', values).then(() => {
            message.success('修改密码成功，请重新登录');
            history.push('/login');
        })
    }

    validator = (rule, value) => {
        return new Promise((resolve, reject) => {
            HttpUtils.get("admin/sysUser/pwd-strength", {password: value}).then(response => {
                const rs = response.data
                if (!rs.success) {
                    reject(rs.message)
                }
                resolve()
            })
        })
    }

    render() {
        if (this.state.checking) return null;

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
                                       {validator: this.validator}
                                   ]}
                        >
                            <Input.Password/>
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

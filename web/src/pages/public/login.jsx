import React from 'react';
import {Button, Form, Input, message} from 'antd';
import {LockOutlined, UserOutlined, WarningOutlined} from '@ant-design/icons';
import {EventBus, HttpClient, GlobalData, history} from "../../framework";

import "./login.less"

function getRedirect(query) {
    let redirect = query?.redirect;
    if (!redirect) {
        redirect = '/'
    }
    return redirect;
}

function postLogin(values, query, success, error) {
    HttpClient.post('/admin/auth/login', values, null, rs => {
        EventBus.emit('loginSuccess')
        history.push(getRedirect(query))
        success && success(rs)
    }, e => {
        console.error('[Login] 登录失败:', e);
        message.error(HttpClient.errToMsg(e))
        error && error(e)
    })
}

function encodePassword(pwd) {
    const chars = [];
    for (let i = 0; i < pwd.length; i++)
        chars.push(pwd.charCodeAt(i) + i + 2);
    return btoa(String.fromCharCode(...chars));
}

export default class LoginPage extends React.Component {

    state = {
        logging: false,
        siteInfo: {}
    }

    componentDidMount() {
        const siteInfo = GlobalData.getSiteInfo()
        if (siteInfo && siteInfo.title) {
            this.setState({siteInfo})
            return
        }
        // localStorage 中无站点信息，从服务端重新加载
        HttpClient.get('/admin/public/site-info', null, rs => {
            GlobalData.setSiteInfo(rs)
            this.setState({siteInfo: rs})
        }, () => {
            console.error('[Login] 加载站点信息失败');
            message.error('加载站点信息失败，请刷新页面重试')
        })
    }

    submit = values => {
        this.setState({logging: true})
        try {
            values.password = encodePassword(values.password)
        } catch (e) {
            console.error('[Login] 密码编码失败:', e);
            this.setState({logging: false})
            message.error('密码含不支持字符，请联系管理员重置')
            return
        }
        postLogin(values, this.props.location?.query, () => {
            this.setState({logging: false})
        }, () => {
            this.setState({logging: false})
        })
    }

    render() {
        const {siteInfo} = this.state

        const pageStyle = {
            backgroundImage: `url("./login_bg.jpg")`
        }

        return (
            <section className='login-page' style={pageStyle}>
                <div className="login-content">
                    <h1>{siteInfo.title}</h1>
                    {this.getForm()}
                    {this.renderFormBottom()}
                </div>
            </section>
        );
    }

    getForm = () => {
        const form = (
            <Form
                name="normal_login"
                className="login-form"
                initialValues={{remember: true}}
                onFinish={this.submit}
                requiredMark={false}
                colon={false}
            >
                <Form.Item name="username" rules={[{required: true, message: '请输入用户名!'}]}>
                    <Input size='large' prefix={<UserOutlined/>} placeholder="用户名" autoComplete="off"/>
                </Form.Item>
                <Form.Item name="password" rules={[{required: true, message: '请输入密码!'}]}>
                    <Input autoComplete="off" prefix={<LockOutlined/>} type="password" placeholder="密码"
                           size='large'
                    />
                </Form.Item>

                <Form.Item style={{marginTop: 10}}>
                    <Button loading={this.state.logging} type="primary" htmlType="submit"
                            block size='large'>
                        登录
                    </Button>
                </Form.Item>
            </Form>
        );

        if (this.props.formRender) {
            return this.props.formRender(form);
        }
        return form;
    };

    renderFormBottom() {
        const siteInfo = this.state.siteInfo;
        if (siteInfo.loginBoxBottomTip) {
            return (
                <div style={{color: 'white', marginTop: 50, fontSize: '14px', textAlign: 'center'}}>
                    <WarningOutlined/> {siteInfo.loginBoxBottomTip}
                </div>
            )
        }
    }
}

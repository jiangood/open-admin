import React from 'react';
import {Button, Form, Input, message} from 'antd';
import {LockOutlined, UserOutlined, WarningOutlined} from '@ant-design/icons';
import {EventBus, HttpUtils, GlobalData, history} from "../framework";

import "./login.less"

function getRedirect(query) {
    let redirect = query?.redirect;
    if (redirect) {
        redirect = decodeURIComponent(redirect)
    } else {
        redirect = '/'
    }
    return redirect;
}

function postLogin(values, query) {
    return new Promise((resolve, reject) => {
        HttpUtils.post('/admin/auth/login', values).then(rs => {
            EventBus.emit('loginSuccess')
            history.push(getRedirect(query))
            resolve(rs)
        }).catch(e => {
            console.error('[Login] 登录失败:', e);
            reject(e)
        })
    })
}

function encodePassword(pwd) {
    const chars = [];
    for (let i = 0; i < pwd.length; i++)
        chars.push(pwd.charCodeAt(i) + i + 2);
    return btoa(String.fromCharCode(...chars));
}

export default class extends React.Component {

    state = {
        logging: false,
        siteInfo: {}
    }

    async componentDidMount() {
        const siteInfo = GlobalData.getSiteInfo()
        if (siteInfo && siteInfo.title) {
            this.setState({siteInfo})
            return
        }
        // localStorage 中无站点信息，从服务端重新加载
        try {
            const rs = await HttpUtils.get('/admin/public/site-info', null, { showError: false })
            GlobalData.setSiteInfo(rs)
            this.setState({siteInfo: rs})
        } catch (e) {
            console.error('[Login] 加载站点信息失败:', e);
            message.error('加载站点信息失败，请刷新页面重试')
        }
    }

    submit = values => {
        this.setState({logging: true})
        values.password = encodePassword(values.password)
        postLogin(values, this.props.location?.query).finally(() => {
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
                    {this.getForm(siteInfo)}
                    {this.renderFormBottom()}
                </div>
            </section>
        );
    }

    getForm = siteInfo => {
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

import React from 'react';
import {Button, Form, Input, message, Space} from 'antd';
import {LockOutlined, SafetyCertificateOutlined, UserOutlined, WarningOutlined} from '@ant-design/icons';
import "./LoginPage.less"
import {MessageUtils, SysUtils} from "../utils";
import {JSEncrypt} from "jsencrypt";
import {LoginPageUtils} from "./LoginPageUtils";


export class LoginPage extends React.Component {


    state = {
        logging: false,

        siteInfo: {},
        random: Math.random()
    }

    async componentDidMount() {
        console.log('渲染登录页面')


        if (localStorage.length === 0) {
            MessageUtils.alert('站点数据缺失，刷新当前页面...')
            window.location.reload()
            return
        }

        const siteInfo = SysUtils.getSiteInfo()
        this.setState({siteInfo})
    }


    submit = values => {
        this.setState({logging: true})

        const pubkey = this.state.siteInfo.rsaPublicKey;
        if (!pubkey) {
            message.error("未获取密钥，请刷新浏览器再试")
            return
        }
        // 对密码加密
        const crypt = new JSEncrypt();
        crypt.setPublicKey(pubkey);
        values.password = crypt.encrypt(values.password)

        LoginPageUtils.postLogin(values).finally(()=>{
            this.setState({logging: false})
        })
    }


    render() {
        const {siteInfo} = this.state

        const pageStyle = {}
        if (siteInfo.loginBackground) {
            let url = 'admin/sysFile/preview/' + siteInfo.loginBackground;
            pageStyle.backgroundImage = 'url("' + url + '")'
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


        const form = <Form
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


            {siteInfo.captcha && <Form.Item name='captchaCode' rules={[{required: true}]}>
                <Space style={{alignItems: 'center'}}>
                    <Input size='large' placeholder='验证码' prefix={<SafetyCertificateOutlined/>}/>
                    <img height={36}
                         width={100}
                         src={"/admin/auth/captchaImage?_random=" + this.state.random}
                         onClick={() => {
                             this.setState({random: Math.random()})
                         }}></img>
                </Space>
            </Form.Item>}


            <Form.Item style={{marginTop: 10}}>
                <Button loading={this.state.logging} type="primary" htmlType="submit"
                        block size='large'>
                    登录
                </Button>
            </Form.Item>
        </Form>;

        if(this.props.formRender){
            return this.props.formRender(form);
        }
        return form;
    };

    renderFormBottom() {
        let siteInfo = this.state.siteInfo;
        if (siteInfo.loginBoxBottomTip) {
            return <div style={{color: 'white', marginTop: 50, fontSize: '14px', textAlign: 'center'}}>
                <WarningOutlined/> {siteInfo.loginBoxBottomTip}
            </div>
        }
    }

}

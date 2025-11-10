import React from 'react';
import {Button, Form, Input, message, Space} from 'antd';
import {LockOutlined, SafetyCertificateOutlined, UserOutlined, WarningOutlined} from '@ant-design/icons';
import "./login.less"
import {history} from 'umi';
import {HttpUtil, PageUtil, SysUtil} from "@tmgg/tmgg-base";
import {JSEncrypt} from "jsencrypt";
import {MsgBox} from "../components/MsgBox";


export default class login extends React.Component {


    state = {
        logging: false,

        siteInfo: {}
    }

    async componentDidMount() {
        console.log('渲染登录页面')
        {
            // 内部系统登录
            let token = PageUtil.currentParams().token
            if (token) {
                token = window.location.search
                this.submit({token})
            }
        }

        if (localStorage.length === 0) {
            MsgBox.toast('客户端数据缺失，需要刷新当前页面')
            window.location.reload()
            return
        }

        const siteInfo = SysUtil.getSiteInfo()
        this.setState({siteInfo})
    }


    submit = values => {
        this.setState({logging: true})

        const crypt = new JSEncrypt();
        const pubkey = this.state.siteInfo.rsaPublicKey;
        if(!pubkey){
            message.error("未获取密钥，请刷新浏览器再试")
            return
        }
        crypt.setPublicKey(pubkey);
        values.password = crypt.encrypt(values.password)

        HttpUtil.post('/auth/login', values).then(rs => {
            console.log('登录结果', rs)
            debugger
            history.push('/')
        }).catch(e=>{
            debugger
        })
            .finally(() => {
            this.setState({logging: false})
        })
    }


    render() {
        const {siteInfo} = this.state

        const pageStyle = {}
        if(siteInfo.loginBackground){
            let url = SysUtil.wrapServerUrl('sysFile/preview/' + siteInfo.loginBackground);
            pageStyle.backgroundImage = 'url("'+url+'")'
        }

        return (
            <section className='login-page' style={pageStyle}>
                <div className="login-content">
                    <h1>{siteInfo.title}</h1>
                    <Form
                        name="normal_login"
                        className="login-form"
                        initialValues={{remember: true}}
                        onFinish={this.submit}
                        requiredMark={false}
                        colon={false}
                    >

                        <Form.Item name="account" rules={[{required: true, message: '请输入用户名!'}]}>
                            <Input size='large' prefix={<UserOutlined/>} placeholder="用户名" autoComplete="off"/>
                        </Form.Item>
                        <Form.Item name="password" rules={[{required: true, message: '请输入密码!'}]}>
                            <Input autoComplete="off" prefix={<LockOutlined/>} type="password" placeholder="密码"
                                   size='large'
                            />
                        </Form.Item>


                        {siteInfo.captcha && <Form.Item name='code' rules={[{required: true}]}>
                            <Space style={{alignItems: 'center'}}>
                                <Input size='large' placeholder='验证码' prefix={<SafetyCertificateOutlined/>}/>
                                <img height={36} width={100}
                                     src={SysUtil.getServerUrl() + "captchaImage?_r=" + this.state.r} onClick={() => {
                                    this.setState({r: Math.random()})
                                }}></img>
                            </Space>
                        </Form.Item>}


                        <Form.Item style={{marginTop: 10}}>
                            <Button loading={this.state.logging} type="primary" htmlType="submit"
                                    block size='large'>
                                登录
                            </Button>
                        </Form.Item>
                    </Form>

                    {this.renderFormBottom()}

                </div>
            </section>
        );
    }


    renderFormBottom() {
        let siteInfo = this.state.siteInfo;
        if (siteInfo.loginBoxBottomTip) {
            return <div style={{color: 'white', marginTop: 50, fontSize: '14px', textAlign: 'center'}}>
                <WarningOutlined/> {siteInfo.loginBoxBottomTip}
            </div>
        }
    }

}

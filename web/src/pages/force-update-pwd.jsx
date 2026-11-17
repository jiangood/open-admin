import React from "react";
import {Button, Form, Input} from "antd";
import {LockOutlined, SafetyCertificateOutlined} from '@ant-design/icons';
import {getMessageApi, GlobalData, HttpClient, history, LOGIN_PATH} from "../../framework";

import "./force-update-pwd.less";

export const pageMeta = {
    requiresAuth: true,
    layout: 'blank',
    title: '修改密码',
};

export default class ForceUpdatePwdPage extends React.Component {

    state = {
        submitting: false,
        siteInfo: GlobalData.getSiteInfo() || {},
        logoError: false,
    };

    componentDidMount() {
        const {siteInfo} = this.state;
        if (siteInfo?.title) {
            return;
        }
        HttpClient.get('/admin/public/site-info', null, {toastError: false}).then(rs => {
            GlobalData.setSiteInfo(rs.data);
            this.setState({siteInfo: rs.data || {}});
        }).catch(() => {
        });
    }

    onFinish = (values) => {
        this.setState({submitting: true});
        HttpClient.post('admin/user-center/update-pwd', values, null).then(() => {
            getMessageApi().success('修改密码成功，请重新登录');
            history.push(LOGIN_PATH);
        }).finally(() => {
            this.setState({submitting: false});
        });
    };

    validator = (rule, value) => {
        if (!value) {
            return Promise.resolve();
        }
        return HttpClient.get("admin/sys-user/pwd-strength", {password: value}, {toastError: false})
            .catch(e => {
                throw new Error(e.message);
            });
    };

    renderBrand() {
        const {siteInfo = {}, logoError} = this.state;
        return (
            <div className="force-pwd-brand">
                <span className="force-pwd-brand-logo">
                    {logoError
                        ? <SafetyCertificateOutlined/>
                        : <img src="./logo.png" alt="" onError={() => this.setState({logoError: true})}/>}
                </span>
                {siteInfo.title && <p className="force-pwd-site-title">{siteInfo.title}</p>}
                <h1>修改密码</h1>
                <p className="force-pwd-desc">首次登录或密码已被重置，请修改密码后重新登录</p>
            </div>
        );
    }

    render() {
        const {submitting} = this.state;
        return (
            <section className='force-pwd-page' style={{backgroundImage: 'url("./login_bg.jpg")'}}>
                <div className="force-pwd-content">
                    {this.renderBrand()}
                    <Form
                        className="force-pwd-form"
                        layout="vertical"
                        onFinish={this.onFinish}
                        requiredMark={false}
                    >
                        <Form.Item
                            name='newPassword'
                            label='新密码'
                            extra='请输入字母、数字、特殊字符'
                            rules={[
                                {required: true, message: '请输入新密码'},
                                {pattern: /^[\x20-\x7E]{1,64}$/, message: '密码仅支持英文、数字与常见符号，长度不超过64位'},
                                {validator: this.validator}
                            ]}
                        >
                            <Input.Password
                                size="large"
                                prefix={<LockOutlined/>}
                                placeholder="请输入新密码"
                                maxLength={64}
                                autoComplete="new-password"
                            />
                        </Form.Item>
                        <Form.Item className="force-pwd-submit">
                            <Button loading={submitting} type="primary" htmlType="submit" block size='large'>
                                确定
                            </Button>
                        </Form.Item>
                    </Form>
                </div>
            </section>
        );
    }
}

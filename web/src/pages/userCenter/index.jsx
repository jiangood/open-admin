import React from "react";
import {Button, Card, Descriptions, Form, Input, Modal} from "antd";
import PermView from "./permView";
import {HttpClient, history, Page} from "../../framework";

export default class UserCenterPage extends React.Component {

    formRef = React.createRef();

    state = {
        info: {},
        changePwdOpen: false,
        changePwdSuccess: false,
    }

    componentDidMount() {
        HttpClient.get('admin/userCenter/info', null, rs => {
            this.setState({info: rs})
        })
    }

    onPwdFinish = (values) => {
        HttpClient.post('admin/userCenter/update-pwd', values, null, () => {
            this.setState({changePwdOpen: false, changePwdSuccess: true});
        })
    }

    pwdValidator = (rule, value) => new Promise((resolve, reject) => {
        HttpClient.get("admin/sysUser/pwd-strength", {password: value}, resolve, (e) => reject(new Error(e.message)))
    })

    render() {
        const {info, changePwdOpen, changePwdSuccess} = this.state;
        return <Page backgroundGray>

            <Card title="个人信息"
                  style={{marginBottom: 16}}
                  extra={<Button type="primary" danger onClick={() => this.setState({changePwdOpen: true})}>修改密码</Button>}>
                <Descriptions column={2} size="middle">
                    <Descriptions.Item label="用户名称">{info.name}</Descriptions.Item>
                    <Descriptions.Item label="账号">{info.account}</Descriptions.Item>
                    <Descriptions.Item label="手机号码">{info.phone}</Descriptions.Item>
                    <Descriptions.Item label="用户邮箱">{info.email}</Descriptions.Item>
                    <Descriptions.Item label="所属单位">{info.unit}</Descriptions.Item>
                    <Descriptions.Item label="所属机构">{info.org}</Descriptions.Item>
                    <Descriptions.Item label="所属角色">{(info.roles || []).join('、') || '-'}</Descriptions.Item>
                    <Descriptions.Item label="创建日期">{info.createTime}</Descriptions.Item>
                </Descriptions>
            </Card>

            <Card title="我的权限">
                <PermView/>
            </Card>

            <Modal open={changePwdOpen} title="修改密码" okText="确定" cancelText="取消"
                   onCancel={() => this.setState({changePwdOpen: false})}
                   onOk={() => this.formRef.current?.submit()}>
                <Form ref={this.formRef} onFinish={this.onPwdFinish} style={{maxWidth: 400}}>
                    <Form.Item name='oldPassword'
                               label='原密码'
                               rules={[{required: true, message: '请输入原密码'}]}
                    >
                        <Input.Password></Input.Password>
                    </Form.Item>
                    <Form.Item name='newPassword'
                               label='新密码'
                               extra={'请输入字母、数字、特殊字符'}
                               rules={[
                                   {required: true, message: '请输入新密码'},
                                   {pattern: /^[\x20-\x7E]{1,64}$/, message: '密码仅支持英文、数字与常见符号，长度不超过64位'},
                                   {
                                       validator: this.pwdValidator
                                   }
                               ]}
                    >
                        <Input.Password maxLength={64}></Input.Password>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal open={changePwdSuccess} title="提示" okText="确定"
                   onCancel={() => this.setState({changePwdSuccess: false})}
                   onOk={() => {
                       this.setState({changePwdSuccess: false});
                       history.push('/public/login');
                   }}>
                修改密码成功
            </Modal>

        </Page>
    }
}

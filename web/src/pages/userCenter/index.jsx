import React from "react";
import {Avatar, Button, Card, Descriptions, Form, Input, Modal, Space} from "antd";
import {EditOutlined, UserOutlined} from "@ant-design/icons";
import PermView from "./permView";
import AvatarPicker from "./avatarPicker";
import {
    AvatarUtils,
    EventBus,
    GlobalData,
    HttpClient,
    history,
    Page,
} from "../../framework";

export default class UserCenterPage extends React.Component {

    formRef = React.createRef();
    editFormRef = React.createRef();

    state = {
        info: {},
        changePwdOpen: false,
        changePwdSuccess: false,
        editOpen: false,
        editSaving: false,
    }

    componentDidMount() {
        HttpClient.get('admin/userCenter/info', null).then(rs => {
            this.setState({info: rs.data})
        })
    }

    onPwdFinish = (values) => {
        HttpClient.post('admin/userCenter/update-pwd', values, null).then(() => {
            this.setState({changePwdOpen: false, changePwdSuccess: true});
        })
    }

    pwdValidator = (rule, value) => HttpClient.get("admin/sysUser/pwd-strength", {password: value}, {toastError: false})
        .catch(e => {
            throw new Error(e.message)
        })

    openEdit = () => {
        const {info} = this.state;
        this.setState({editOpen: true}, () => {
            this.editFormRef.current?.setFieldsValue({
                email: info.email,
                avatar: info.avatar,
            });
        });
    }

    onEditFinish = (values) => {
        const data = {
            email: values.email || '',
            avatar: values.avatar || '',
        };
        this.setState({editSaving: true});
        HttpClient.post('admin/userCenter/update-info', data, null).then(() => {
            this.setState(prevState => ({
                info: {...prevState.info, ...data},
                editOpen: false,
            }));
            // 同步顶栏头像
            GlobalData.setLoginInfo({...GlobalData.getLoginInfo(), avatar: data.avatar});
            EventBus.emit('loginInfoChange');
        }).finally(() => {
            this.setState({editSaving: false});
        });
    }

    render() {
        const {info, changePwdOpen, changePwdSuccess, editOpen, editSaving} = this.state;
        const avatarSrc = AvatarUtils.resolve(info.avatar);

        return <Page>

            <Card title="个人信息"
                  style={{marginBottom: 16}}
                  extra={<Space>
                      <Button icon={<EditOutlined/>} onClick={this.openEdit}>编辑资料</Button>
                      <Button type="primary" danger onClick={() => this.setState({changePwdOpen: true})}>修改密码</Button>
                  </Space>}>
                <div style={{display: 'flex', alignItems: 'center', gap: 16, marginBottom: 20}}>
                    <Avatar size={64} src={avatarSrc} icon={<UserOutlined/>}/>
                    <div>
                        <div style={{fontSize: 18, fontWeight: 600}}>{info.name}</div>
                        <div style={{color: 'var(--oa-color-text-secondary, #666)'}}>{info.account}</div>
                    </div>
                </div>

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

            <Modal open={editOpen} title="编辑资料" okText="保存" cancelText="取消"
                   confirmLoading={editSaving}
                   onCancel={() => this.setState({editOpen: false})}
                   onOk={() => this.editFormRef.current?.submit()}>
                <Form ref={this.editFormRef} onFinish={this.onEditFinish} layout="vertical"
                      style={{maxWidth: 400}}>
                    <Form.Item label="头像" name="avatar">
                        <AvatarPicker/>
                    </Form.Item>
                    <Form.Item name="email"
                               label="用户邮箱"
                               rules={[
                                   {type: 'email', message: '请输入正确的邮箱地址'},
                                   {max: 30, message: '邮箱长度不能超过30位'},
                               ]}
                    >
                        <Input placeholder="请输入邮箱" maxLength={30}/>
                    </Form.Item>
                </Form>
            </Modal>

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

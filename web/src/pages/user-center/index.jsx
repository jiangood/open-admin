import React from "react";
import {
    Alert,
    Avatar,
    Button,
    Card,
    Col,
    Form,
    Input,
    Modal,
    Popconfirm,
    Row,
    Space,
    Table,
    Tag,
    Tooltip,
} from "antd";
import {EditOutlined, UserOutlined} from "@ant-design/icons";
import PermView from "./PermView";
import AvatarPicker from "./AvatarPicker";
import {
    AvatarUtils,
    EventBus,
    GlobalData,
    HttpClient,
    history,
    LOGIN_PATH,
    Page,
} from "../../framework";

export const pageMeta = {
    title: '个人中心',
};

const metaLabelStyle = {
    marginBottom: 2,
    fontSize: 12,
    color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))',
    whiteSpace: 'nowrap',
};
const metaValueStyle = {fontSize: 14, wordBreak: 'break-word'};

/**
 * 从 User-Agent 粗略识别浏览器 / 操作系统，识别不出时显示原始 UA 摘要。
 * 仅用于个人中心展示，不追求完整设备库。
 */
const parseDevice = (ua) => {
    if (!ua) {
        return '未知设备';
    }

    const browser = /MicroMessenger/.test(ua) ? '微信'
        : /Edg\//.test(ua) ? 'Edge'
            : /OPR\/|Opera/.test(ua) ? 'Opera'
                : /Firefox\//.test(ua) ? 'Firefox'
                    : /Chrome\/|Chromium/.test(ua) ? 'Chrome'
                        : /Safari\//.test(ua) ? 'Safari'
                            : '浏览器';

    const os = /Windows NT/.test(ua) ? 'Windows'
        : /Android/.test(ua) ? 'Android'
            : /(iPhone|iPad|iPod)/.test(ua) ? 'iOS'
                : /Mac OS X/.test(ua) ? 'macOS'
                    : /Linux/.test(ua) ? 'Linux'
                        : '';

    return os ? `${browser} / ${os}` : browser;
};

export default class UserCenterPage extends React.Component {

    formRef = React.createRef();
    editFormRef = React.createRef();

    state = {
        info: {},
        sessions: [],
        sessionModalOpen: false,
        sessionLoading: false,
        changePwdOpen: false,
        changePwdSuccess: false,
        editOpen: false,
        editSaving: false,
    }

    componentDidMount() {
        HttpClient.get('admin/user-center/info', null).then(rs => {
            this.setState({info: rs.data})
        })
        this.loadSessions();
    }

    loadSessions = (showLoading = false) => {
        if (showLoading) {
            this.setState({sessionLoading: true});
        }
        return HttpClient.get('admin/user-center/sessions', null, {toastError: false})
            .then(rs => {
                const sessions = rs.data || [];
                this.setState({sessions});
                return sessions;
            })
            .catch(() => {
                // 会话查询失败不打扰用户，个人中心其它信息照常展示
                this.setState({sessions: []});
                return [];
            })
            .finally(() => {
                if (showLoading) {
                    this.setState({sessionLoading: false});
                }
            });
    }

    openSessions = () => {
        this.setState({sessionModalOpen: true});
        this.loadSessions(true);
    }

    closeSessions = () => {
        this.setState({sessionModalOpen: false});
    }

    kickSession = (id) => {
        HttpClient.post('admin/user-center/sessions/kick', {id})
            .then(() => this.loadSessions(true))
            .then(sessions => {
                // 只剩当前设备时没有必要继续展示详情
                this.setState({sessionModalOpen: sessions.length > 1});
            })
            .catch(() => {
            });
    }

    onPwdFinish = (values) => {
        HttpClient.post('admin/user-center/update-pwd', values, null).then(() => {
            this.setState({changePwdOpen: false, changePwdSuccess: true});
        })
    }

    pwdValidator = (rule, value) => HttpClient.get("admin/sys-user/pwd-strength", {password: value}, {toastError: false})
        .catch(e => {
            throw new Error(e.message)
        })

    openEdit = () => {
        this.setState({editOpen: true});
    }

    onEditFinish = (values) => {
        const data = {
            phone: values.phone?.trim() || '',
            email: values.email || '',
            avatar: values.avatar || '',
        };
        this.setState({editSaving: true});
        HttpClient.post('admin/user-center/update-info', data, null).then(() => {
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

    renderInfoItem = (label, value, cellStyle, valueStyle) => (
        <div style={{minWidth: 0, ...cellStyle}}>
            <div style={metaLabelStyle}>{label}</div>
            <div style={{...metaValueStyle, ...valueStyle}}>{value || '-'}</div>
        </div>
    );

    sessionColumns = [
        {
            title: '设备',
            dataIndex: 'userAgent',
            render: (ua) => {
                const label = parseDevice(ua);
                return ua ? <Tooltip title={ua}><span>{label}</span></Tooltip> : label;
            },
        },
        {
            title: 'IP',
            dataIndex: 'ip',
            width: 170,
            render: (v, record) => (
                <span>
                    {v || '-'}
                    {record.innerIp && <Tag style={{marginInlineStart: 6, marginInlineEnd: 0}}>内网</Tag>}
                </span>
            ),
        },
        {
            title: '登录时间',
            dataIndex: 'loginTime',
            width: 170,
            render: v => v || '-',
        },
        {
            title: '最后活跃',
            dataIndex: 'lastActiveTime',
            width: 170,
            render: v => v || '-',
        },
        {
            title: '操作',
            dataIndex: 'option',
            width: 110,
            render: (_, record) => record.current
                ? <Tag color="green" style={{marginInlineEnd: 0}}>当前设备</Tag>
                : <Popconfirm title="确定下线该设备吗？"
                              okText="下线"
                              cancelText="取消"
                              onConfirm={() => this.kickSession(record.id)}>
                    <Button size="small" danger>下线</Button>
                </Popconfirm>,
        },
    ];

    render() {
        const {
            info,
            sessions,
            sessionModalOpen,
            sessionLoading,
            changePwdOpen,
            changePwdSuccess,
            editOpen,
            editSaving,
        } = this.state;
        const avatarSrc = AvatarUtils.resolve(info.avatar);
        const unitOrg = [info.unit, info.org].filter(Boolean).join(' / ') || '-';

        return <Page>

            {sessions.length > 1 && <Alert
                type="warning"
                showIcon
                style={{marginBottom: 16}}
                title={`检测到你的账号当前在 ${sessions.length} 处登录`}
                description="如非本人操作，请查看详情并下线其他设备，必要时修改密码。"
                action={<Button size="small" onClick={this.openSessions}>查看详情</Button>}
            />}

            <Card title="个人信息"
                  style={{marginBottom: 16}}
                  extra={<Space>
                      <Button type="primary" icon={<EditOutlined/>} onClick={this.openEdit}>编辑资料</Button>
                      <Button onClick={() => this.setState({changePwdOpen: true})}>修改密码</Button>
                  </Space>}>
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1fr)',
                    columnGap: 32,
                    rowGap: 20,
                    alignItems: 'start',
                }}>
                    <div style={{display: 'flex', alignItems: 'center', gap: 16, minWidth: 0}}>
                        <Avatar size={72} src={avatarSrc || undefined} icon={<UserOutlined/>}/>
                        <div style={{minWidth: 0}}>
                            <div style={{display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', minWidth: 0}}>
                                <span style={{
                                    fontSize: 18,
                                    fontWeight: 600,
                                    lineHeight: 1.4,
                                    wordBreak: 'break-word',
                                }}>{info.name || '-'}</span>
                                {(info.roles || []).map(role =>
                                    <Tag key={role} style={{marginInlineEnd: 0}}>{role}</Tag>
                                )}
                            </div>
                            <div style={{
                                marginTop: 4,
                                fontSize: 13,
                                color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))',
                                wordBreak: 'break-all',
                            }}>
                                账号：{info.account || '-'}
                            </div>
                        </div>
                    </div>

                    {this.renderInfoItem('所属单位/部门', unitOrg, {alignSelf: 'center'})}
                    {this.renderInfoItem('手机', info.phone)}
                    {this.renderInfoItem('邮箱', info.email, null, {wordBreak: 'break-all'})}
                    {this.renderInfoItem('上次登录', info.lastLoginTime)}
                    {this.renderInfoItem('注册时间', info.createTime)}
                </div>
            </Card>

            <Card>
                <PermView/>
            </Card>

            <Modal open={sessionModalOpen} title="登录设备" width={900}
                   footer={<Button onClick={this.closeSessions}>关闭</Button>}
                   onCancel={this.closeSessions}>
                <div style={{
                    marginBottom: 12,
                    fontSize: 13,
                    color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))',
                }}>
                    当前账号共 {sessions.length} 处登录，可下线非当前设备。
                </div>
                <Table rowKey="id"
                       size="middle"
                       loading={sessionLoading}
                       columns={this.sessionColumns}
                       dataSource={sessions}
                       pagination={false}
                       locale={{emptyText: '暂无登录设备'}}/>
            </Modal>

            {/* destroyOnHidden：每次打开重建表单，配合 initialValues 回填当前资料（头像/手机/邮箱） */}
            <Modal open={editOpen} title="编辑资料" okText="保存" cancelText="取消"
                   width={560}
                   destroyOnHidden
                   confirmLoading={editSaving}
                   onCancel={() => this.setState({editOpen: false})}
                   onOk={() => this.editFormRef.current?.submit()}>
                <Form ref={this.editFormRef} onFinish={this.onEditFinish} layout="vertical"
                      initialValues={{phone: info.phone, email: info.email, avatar: info.avatar}}
                      style={{paddingTop: 4}}>
                    <Form.Item label="头像" name="avatar"
                               rules={[{required: true, message: '请选择或上传头像'}]}>
                        <AvatarPicker/>
                    </Form.Item>
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item name="phone"
                                       label="手机号码"
                                       rules={[
                                           {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号'},
                                           {max: 11, message: '手机号长度不能超过11位'},
                                       ]}
                            >
                                <Input placeholder="请输入手机号" maxLength={11} allowClear/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="email"
                                       label="用户邮箱"
                                       rules={[
                                           {type: 'email', message: '请输入正确的邮箱地址'},
                                           {max: 30, message: '邮箱长度不能超过30位'},
                                       ]}
                            >
                                <Input placeholder="请输入邮箱" maxLength={30} allowClear/>
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </Modal>

            <Modal open={changePwdOpen} title="修改密码" okText="确定" cancelText="取消"
                   width={480}
                   onCancel={() => this.setState({changePwdOpen: false})}
                   onOk={() => this.formRef.current?.submit()}>
                <Form ref={this.formRef} onFinish={this.onPwdFinish} layout="vertical"
                      style={{paddingTop: 4}}>
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
                       history.push(LOGIN_PATH);
                   }}>
                修改密码成功
            </Modal>

        </Page>
    }
}

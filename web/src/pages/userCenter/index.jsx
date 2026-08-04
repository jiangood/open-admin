import React from "react";
import {Avatar, Card, Descriptions, Space, Tag, Tabs, Typography} from "antd";
import ChangePassword from "./changePassword";
import PermView from "./permView";
import {HttpUtils, Page} from "../../framework";

export default class extends React.Component {

    state = {
        info: {}
    }

    componentDidMount() {
        HttpUtils.get('admin/userCenter/info').then(rs => {
            this.setState({info: rs})
        })
    }

    render() {
        const {info} = this.state;
        return <Page backgroundGray title="个人中心" description="查看和编辑个人资料">

            <Card style={{marginBottom: 16}} styles={{body: {padding: 0}}}>
                <div style={{
                    background: 'linear-gradient(135deg, #1677ff 0%, #69b1ff 100%)',
                    padding: '40px 24px 56px',
                    textAlign: 'center',
                }}>
                    <Avatar size={96} title='点击修改头像' style={{backgroundColor: 'rgba(255,255,255,0.25)', fontSize: 40}}>
                        {info.name ? info.name.charAt(0) : 'U'}
                    </Avatar>
                    <div style={{marginTop: 16}}>
                        <span style={{fontSize: 22, fontWeight: 600, color: '#fff'}}>{info.name}</span>
                    </div>
                    <Typography.Text style={{color: 'rgba(255,255,255,0.85)'}}>账号：{info.account}</Typography.Text>
                    <div style={{marginTop: 8}}>
                        <Space wrap>
                            {(info.roles || []).map((r, i) => <Tag color="blue" key={i}>{r}</Tag>)}
                            {info.org ? <Tag color="cyan">{info.org}</Tag> : null}
                        </Space>
                    </div>
                </div>
            </Card>

            <Card>
                <Tabs
                    items={[
                        {
                            label: '个人信息', key: 'info', children: (
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
                            )
                        },
                        {
                            label: '我的权限', key: 'perms', children: (
                                <div>
                                    <PermView/>
                                </div>
                            )
                        },
                        {
                            label: '修改密码', key: 'pwd', children: (
                                <div>
                                    <ChangePassword/>
                                </div>
                            )
                        },
                    ]}
                />
            </Card>

        </Page>
    }
}

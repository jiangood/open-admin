import React from "react";
import {Button, Card, Descriptions} from "antd";
import ChangePassword from "./changePassword";
import PermView from "./permView";
import {HttpUtils, Page} from "../../framework";

export default class extends React.Component {

    state = {
        info: {},
        changePwdOpen: false,
    }

    componentDidMount() {
        HttpUtils.get('admin/userCenter/info').then(rs => {
            this.setState({info: rs})
        })
    }

    render() {
        const {info, changePwdOpen} = this.state;
        return <Page backgroundGray>

            <Card title="个人信息"
                  style={{marginBottom: 16}}
                  extra={<Button onClick={() => this.setState({changePwdOpen: true})}>修改密码</Button>}>
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

            <Card>
                <PermView/>
            </Card>

            <ChangePassword open={changePwdOpen} onClose={() => this.setState({changePwdOpen: false})}/>

        </Page>
    }
}

import React from "react";
import {Avatar, Card, Col, Row, Tabs} from "antd";
import ChangePassword from "./ChangePassword";
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
        return <Page padding backgroundGray>
            <Row gutter={[16, 16]}>
                <Col md={6} sm={24}>
                    <Card title='个人信息'>

                        <div className='flex-center' style={{marginBottom: 32}}>
                            <Avatar size={128} title='点击修改头像'/>
                        </div>


                        <table className='tmgg-table'>
                            <tbody>
                            <tr>
                                <td>用户名称</td>
                                <td align='right'>{info.name}</td>
                            </tr>
                            <tr>
                                <td>账号</td>
                                <td align='right'>{info.account}</td>
                            </tr>
                            <tr>
                                <td>手机号码</td>
                                <td>{info.phone}</td>
                            </tr>
                            <tr>
                                <td>用户邮箱</td>
                                <td>{info.email}</td>
                            </tr>
                            <tr>
                                <td>所属部门</td>
                                <td>{info.unit} {info.dept}</td>
                            </tr>
                            <tr>
                                <td>所属角色</td>
                                <td>{info.roles}</td>
                            </tr>
                            <tr>
                                <td>创建日期</td>
                                <td>{info.createTime}</td>
                            </tr>
                            </tbody>
                        </table>
                    </Card>
                </Col>
                <Col md={18} sm={24}>
                    <Card title='个人设置'>
                        <Tabs
                            items={[
                                {
                                    label: '修改密码', key: 'pwd', children: <div>
                                        <ChangePassword/>
                                    </div>
                                },
                                {
                                    label: '消息订阅', key: 'topic', children: <div>
                                        当账号管理邮箱后，系统消息会发送邮件给您
                                    </div>
                                }
                            ]}
                            tabPosition='left'
                        >
                        </Tabs>
                    </Card>
                </Col>
            </Row>


        </Page>

    }
}

import {Badge, Dropdown} from "antd";
import {NotificationOutlined, QuestionCircleOutlined, SettingOutlined, UserOutlined} from "@ant-design/icons";
import React from "react";
import {history} from "umi";
import {HttpUtil, PageUtil, SysUtil} from "@tmgg/tmgg-base";
import {isMobileDevice} from "@tmgg/tmgg-commons-lang";
import {MsgBox} from "../../components/MsgBox";


const ID = 'header-right';
export default class HeaderRight extends React.Component {

    state = {
        messageCount: 0,
        isMobileDevice: false
    };

    componentDidMount() {
        this.initMessage()
        document.dispatchEvent(new CustomEvent('componentDidMount', {detail: ID}))
        if (isMobileDevice()) {
            this.setState({isMobileDevice: true})
        }
    }

    initMessage = () => {
        HttpUtil.get('user/msg/getMessageCount').then(rs => {
            this.setState({messageCount: rs})
        })
    }

    logout = () => {
        HttpUtil.post('/auth/logout').then(async () => {
            localStorage.clear()
            await MsgBox.alert('退出登录成功');
            history.replace('/login')
        }).catch(async e => {
            let confirm = await MsgBox.confirm('退出登录失败，是否清空缓存');
            if (confirm) {
                localStorage.clear();
                history.replace('/login')
            }
        })
    }

    userCenter = () => {
        PageUtil.open('/userCenter', '个人中心')
    }

    render() {
        const info = SysUtil.getLoginInfo()

        if (this.state.isMobileDevice) {
            return <div className='header-right'>
                <a onClick={this.logout}>退出</a>
            </div>
        }

        return <div className='header-right'>

            <div className='item'>
                <UserOutlined/> {info.name}
            </div>


            <div className='item' onClick={() => PageUtil.open('/userCenter/message', '我的消息')}>
                <Badge count={this.state.messageCount} size="small">
                    <NotificationOutlined/>
                </Badge>
            </div>

            <div className='item' title='操作手册' onClick={() => PageUtil.open('/userCenter/manual', '操作手册')}>
                <QuestionCircleOutlined/>
            </div>


            <div className='item'>

                <Dropdown menu={{
                    onClick: ({key}) => {
                        switch (key) {
                            case 'userCenter':
                                this.userCenter()
                                break;
                            case 'logout':
                                this.logout();
                                break;
                            case 'about':
                                this.about()
                                break
                        }
                    },
                    items: [
                        {key: 'userCenter', label: '个人中心'},
                        {key: 'about', label: '关于系统'},
                        {key: 'logout', label: '退出登录'},

                    ]
                }}><SettingOutlined/>

                </Dropdown>

            </div>

        </div>
    }


    about = () => {
        history.push("/about")
    }
}

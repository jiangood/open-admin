import {Badge, Dropdown} from "antd";
import {NotificationOutlined, QuestionCircleOutlined, SettingOutlined, UserOutlined} from "@ant-design/icons";
import React from "react";
import {history} from "umi";
import {DeviceUtils, HttpUtils, MessageUtils,  PageUtils, SysUtils} from "../../framework";


const ID = 'header-right';
export default class HeaderRight extends React.Component {

    state = {
        isMobileDevice: false
    };

    componentDidMount() {
        document.dispatchEvent(new CustomEvent('componentDidMount', {detail: ID}))
        if (DeviceUtils.isMobileDevice()) {
            this.setState({isMobileDevice: true})
        }
    }



    logout = () => {
        HttpUtils.post('admin/auth/logout').then(async () => {
            localStorage.clear()
            await MessageUtils.alert('退出登录成功');
            history.replace('/login')
        }).catch(async e => {
            let confirm = await MessageUtils.confirm('退出登录失败，是否清空缓存');
            if (confirm) {
                localStorage.clear();
                history.replace('/login')
            }
        })
    }

    userCenter = () => {
        PageUtils.open('/userCenter', '个人中心')
    }

    render() {
        const info = SysUtils.getLoginInfo()

        if (this.state.isMobileDevice) {
            return <div className='header-right'>
                <a onClick={this.logout}>退出</a>
            </div>
        }

        return <div className='header-right'>

            <div className='item'>
                <UserOutlined/> {info.name}
            </div>


            <div className='item' onClick={() => PageUtils.open('/userCenter/message', '我的消息')}>
                <Badge count={info.messageCount} size="small">
                    <NotificationOutlined/>
                </Badge>
            </div>

            <div className='item' title='操作手册' onClick={() => PageUtils.open('/userCenter/manual', '操作手册')}>
                <QuestionCircleOutlined />
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

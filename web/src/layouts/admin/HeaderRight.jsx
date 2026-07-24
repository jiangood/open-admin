import {Avatar, Dropdown} from "antd";
import React from "react";
import {history} from "umi";
import {DeviceUtils, HttpUtils, MessageUtils, PageUtils, GlobalData, ThemeUtils} from "../../framework";

export class HeaderRight extends React.Component {

    state = {
        isMobileDevice: false
    };

    componentDidMount() {
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
            console.error('[HeaderRight] 退出登录失败:', e);
            const confirm = await MessageUtils.confirm('退出登录失败，是否清空缓存');
            if (confirm) {
                localStorage.clear();
                history.replace('/login')
            }
        })
    }

    account = () => {
        PageUtils.open('/account', '个人中心')
    }

    render() {
        const info = GlobalData.getLoginInfo()

        if (this.state.isMobileDevice) {
            return <div className='header-right'>
                <a onClick={this.logout}>退出</a>
            </div>
        }

        return <div className='header-right'>

            <Dropdown menu={{
                onClick: ({key}) => {
                    switch (key) {
                        case 'account':
                            this.account()
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
                    {key: 'account', label: '个人中心'},
                    {key: 'about', label: '关于系统'},
                    {key: 'logout', label: '退出登录'},
                ]
            }}>
                <div className='item' style={{cursor: 'pointer'}}>
                    <Avatar size="default" style={{backgroundColor: ThemeUtils.getColor('primary-color')}}>
                        {info.name?.charAt(0)}
                    </Avatar>
                    <span style={{marginLeft: 8}}>{info.name}</span>
                </div>
            </Dropdown>

        </div>
    }


    about = () => {
        history.push("/about")
    }
}

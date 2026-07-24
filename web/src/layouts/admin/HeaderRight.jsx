import {Avatar, Dropdown} from "antd";
import React from "react";
import {history} from "umi";
import {DeviceUtils, HttpUtils, MessageUtils, PageUtils, GlobalData, ThemeUtils} from "../../framework";

export class HeaderRight extends React.Component {

    state = {
        isMobileDevice: false,
        dropdownArticles: [],
        headerArticles: [],
    };

    componentDidMount() {
        if (DeviceUtils.isMobileDevice()) {
            this.setState({isMobileDevice: true})
        }
        this.loadArticles()
    }

    loadArticles = () => {
        HttpUtils.get('admin/article/listByPosition', {position: 'dropdown'}).then(rs => {
            this.setState({dropdownArticles: rs || []})
        })
        HttpUtils.get('admin/article/listByPosition', {position: 'header'}).then(rs => {
            this.setState({headerArticles: rs || []})
        })
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

    openArticle = (code, title) => {
        PageUtils.open('/article/' + code, title)
    }

    render() {
        const info = GlobalData.getLoginInfo()
        const {dropdownArticles, headerArticles} = this.state

        if (this.state.isMobileDevice) {
            return <div className='header-right'>
                <a onClick={this.logout}>退出</a>
            </div>
        }

        const articleItems = dropdownArticles.map(a => ({
            key: 'article:' + a.code,
            label: a.title,
        }))

        const menuItems = [
            {key: 'account', label: '个人中心'},
            ...articleItems,
            {key: 'logout', label: '退出登录'},
        ]

        return <div className='header-right'>
            {headerArticles.map(a => (
                <div key={a.code} className='item' style={{cursor: 'pointer'}}
                     onClick={() => this.openArticle(a.code, a.title)}>
                    {a.title}
                </div>
            ))}

            <Dropdown menu={{
                onClick: ({key}) => {
                    if (key === 'account') {
                        this.account()
                    } else if (key === 'logout') {
                        this.logout();
                    } else if (key.startsWith('article:')) {
                        const code = key.substring(8)
                        const article = dropdownArticles.find(a => a.code === code)
                        this.openArticle(code, article ? article.title : code)
                    }
                },
                items: menuItems,
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
}

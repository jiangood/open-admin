import {Avatar, Dropdown, Modal} from "antd";
import React from "react";
import {DeviceUtils, HttpUtils, PageUtils, GlobalData, getToken, history, EventBus, ARTICLE_HEADER_AVATAR_DROPDOWN, ARTICLE_HEADER_RIGHT} from "../../framework";

export class HeaderRight extends React.Component {

    state = {
        isMobileDevice: false,
        dropdownArticles: [],
        headerArticles: [],
        alertVisible: false,
        confirmVisible: false,
    };

    componentDidMount() {
        if (DeviceUtils.isMobileDevice()) {
            this.setState({isMobileDevice: true})
        }
        this.loadArticles()
    }

    loadArticles = () => {
        const siteArticles = GlobalData.getSiteArticles();
        this.setState({
            dropdownArticles: siteArticles[ARTICLE_HEADER_AVATAR_DROPDOWN] || [],
            headerArticles: siteArticles[ARTICLE_HEADER_RIGHT] || [],
        })
    }

    logout = () => {
        HttpUtils.post('admin/auth/logout').then(async () => {
            localStorage.clear()
            this.setState({alertVisible: true})
        }).catch(async e => {
            console.error('[HeaderRight] 退出登录失败:', e);
            this.setState({confirmVisible: true})
        })
    }

    account = () => {
        PageUtils.open('/userCenter', '个人中心')
    }

    openArticle = (code, title) => {
        PageUtils.open('/article/' + code, title)
    }

    render() {
        const info = GlobalData.getLoginInfo()
        const {dropdownArticles, headerArticles} = this.state

        if (this.state.isMobileDevice) {
            return <div className='header-right'>
                <a role="button" tabIndex={0} onClick={this.logout}
                   onKeyDown={(e) => {
                       if (e.key === 'Enter' || e.key === ' ') {
                           e.preventDefault();
                           this.logout();
                       }
                   }}>退出</a>
            </div>
        }

        const articleItems = dropdownArticles.map(a => ({
            key: 'article:' + a.code,
            label: a.title,
        }))

        const menuItems = [
            {key: 'userCenter', label: '个人中心'},
            ...articleItems,
            {key: 'logout', label: '退出登录'},
        ]

        return <div className='header-right'>
            {headerArticles.map(a => (
                <div key={a.code} className='item' style={{cursor: 'pointer'}} role="button" tabIndex={0}
                     onClick={() => this.openArticle(a.code, a.title)}
                     onKeyDown={(e) => {
                         if (e.key === 'Enter' || e.key === ' ') {
                             e.preventDefault();
                             this.openArticle(a.code, a.title);
                         }
                     }}>
                    {a.title}
                </div>
            ))}

            <Dropdown menu={{
                onClick: ({key}) => {
                    if (key === 'userCenter') {
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
                    <Avatar size="default" style={{backgroundColor: getToken().colorPrimary}}>
                        {info.name?.charAt(0)}
                    </Avatar>
                    <span style={{marginLeft: 8}}>{info.name}</span>
                </div>
            </Dropdown>

            <Modal open={this.state.alertVisible} title="提示" okText="确定"
                   onCancel={() => this.setState({alertVisible: false})}
                   onOk={() => {
                       this.setState({alertVisible: false});
                        EventBus.emit('logoutSuccess');
                        history.replace('/public/login')
                    }}>
                 退出登录成功
            </Modal>
            <Modal open={this.state.confirmVisible} title="确认操作" okText="确定" cancelText="取消"
                   onCancel={() => this.setState({confirmVisible: false})}
                   onOk={() => {
                       this.setState({confirmVisible: false});
                       localStorage.clear();
                       EventBus.emit('logoutSuccess');
                       history.replace('/public/login')
                   }}>
                退出登录失败，是否清空缓存
            </Modal>
        </div>
    }
}

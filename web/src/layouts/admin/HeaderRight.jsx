import {Avatar, Dropdown, Modal} from "antd";
import {BgColorsOutlined, FileOutlined, MoonOutlined, ProfileOutlined, SunOutlined, UserOutlined} from "@ant-design/icons";
import React from "react";
import {HttpClient, PageUtils, GlobalData, history, EventBus, LOGIN_PATH, ThemeSettings, AvatarUtils, ARTICLE_HEADER_AVATAR_DROPDOWN, ARTICLE_HEADER_RIGHT, getThemeMode, toggleThemeMode, getLayoutMode, toggleLayoutMode} from "../../framework";

export class HeaderRight extends React.Component {

    state = {
        dropdownArticles: [],
        headerArticles: [],
        alertVisible: false,
        confirmVisible: false,
        themeSettingsVisible: false,
    };

    unsubscribeLoginInfo = null;
    unsubscribeTheme = null;
    unsubscribeLayout = null;

    componentDidMount() {
        this.loadArticles()
        // 个人中心修改头像/资料后刷新顶栏显示
        this.unsubscribeLoginInfo = EventBus.on('loginInfoChange', () => this.setState({}))
        // 明暗切换后刷新太阳/月亮图标（其它入口切换时也能同步）
        this.unsubscribeTheme = EventBus.on('themeChange', () => this.setState({}))
        // 布局切换后刷新图标（其它入口切换时也能同步）
        this.unsubscribeLayout = EventBus.on('layoutChange', () => this.setState({}))
    }

    componentWillUnmount() {
        if (this.unsubscribeLoginInfo) {
            this.unsubscribeLoginInfo()
        }
        if (this.unsubscribeTheme) {
            this.unsubscribeTheme()
        }
        if (this.unsubscribeLayout) {
            this.unsubscribeLayout()
        }
    }

    loadArticles = () => {
        const siteArticles = GlobalData.getSiteArticles();
        this.setState({
            dropdownArticles: siteArticles[ARTICLE_HEADER_AVATAR_DROPDOWN] || [],
            headerArticles: siteArticles[ARTICLE_HEADER_RIGHT] || [],
        })
    }

    logout = () => {
        HttpClient.post('admin/auth/logout', null, null, {toastError: false}).then(() => {
            localStorage.clear()
            this.setState({alertVisible: true})
        }).catch(e => {
            console.error('[HeaderRight] 退出登录失败:', e);
            this.setState({confirmVisible: true})
        })
    }

    account = () => {
        PageUtils.open('/user-center')
    }

    openArticle = (code) => {
        PageUtils.open('/article/' + code)
    }

    renderThemeModeToggle = () => {
        const isDark = getThemeMode() === 'dark';
        const title = isDark ? '切换到浅色模式' : '切换到深色模式';
        return (
            <button type="button" className='item' title={title} aria-label={title}
                    onClick={() => toggleThemeMode()}>
                {isDark ? <SunOutlined/> : <MoonOutlined/>}
            </button>
        )
    }

    renderLayoutModeToggle = () => {
        const isTabs = getLayoutMode() === 'tabs';
        const title = isTabs ? '切换为单页模式' : '切换为多标签页模式';
        const Icon = isTabs ? FileOutlined : ProfileOutlined;
        return (
            <button type="button" className='item' title={title} aria-label={title}
                    onClick={() => toggleLayoutMode()}>
                <Icon/>
            </button>
        )
    }

    renderThemeSettings = () => {
        return (
            <button type="button" className='item' title="界面设置" aria-label="界面设置"
                    onClick={() => this.setState({themeSettingsVisible: true})}>
                <BgColorsOutlined/>
            </button>
        )
    }

    renderThemeSettingsDrawer = () => {
        return (
            <ThemeSettings open={this.state.themeSettingsVisible}
                           onClose={() => this.setState({themeSettingsVisible: false})}/>
        )
    }

    render() {
        const info = GlobalData.getLoginInfo()
        const avatarSrc = AvatarUtils.resolve(info.avatar)
        const {dropdownArticles, headerArticles} = this.state

        const articleItems = dropdownArticles.map(a => ({
            key: 'article:' + a.code,
            label: a.title,
        }))

        const menuItems = [
            {key: 'user-center', label: '个人中心'},
            ...articleItems,
            {key: 'logout', label: '退出登录'},
        ]

        return <div className='header-right'>
            {headerArticles.map(a => (
                <button key={a.code} type="button" className='item'
                        onClick={() => this.openArticle(a.code)}>
                    {a.title}
                </button>
            ))}

            {this.renderLayoutModeToggle()}

            {this.renderThemeModeToggle()}

            {this.renderThemeSettings()}

            <Dropdown menu={{
                onClick: ({key}) => {
                    if (key === 'user-center') {
                        this.account()
                    } else if (key === 'logout') {
                        this.logout();
                    } else if (key.startsWith('article:')) {
                        this.openArticle(key.substring(8))
                    }
                },
                items: menuItems,
            }}>
                <div className='item' style={{cursor: 'pointer'}}>
                    <Avatar size="default" src={avatarSrc || undefined} icon={<UserOutlined/>}/>
                    <span style={{marginLeft: 8}}>{info.name}</span>
                </div>
            </Dropdown>

            <Modal open={this.state.alertVisible} title="提示" okText="确定"
                   onCancel={() => this.setState({alertVisible: false})}
                   onOk={() => {
                       this.setState({alertVisible: false});
                        EventBus.emit('logoutSuccess');
                        history.replace(LOGIN_PATH)
                    }}>
                 退出登录成功
            </Modal>
            <Modal open={this.state.confirmVisible} title="确认操作" okText="确定" cancelText="取消"
                   onCancel={() => this.setState({confirmVisible: false})}
                   onOk={() => {
                       this.setState({confirmVisible: false});
                       localStorage.clear();
                       EventBus.emit('logoutSuccess');
                       history.replace(LOGIN_PATH)
                   }}>
                退出登录失败，是否清空缓存
            </Modal>

            {this.renderThemeSettingsDrawer()}
        </div>
    }
}

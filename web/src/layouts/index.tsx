import React from "react";
import {App, ConfigProvider, Modal} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {ErrorBoundary, GlobalData, HttpClient, PageFrame, PageLoading, PageUtils, history, getThemeConfig, getThemeMode, setThemeColors, setMessageApi, EventBus} from "../framework";
import type {ThemeColors} from "../framework";

import AdminLayout from "./admin"

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

const baseConfigProps = {
    input: {autoComplete: 'off'},
    form: {validateMessages: {required: '必填项'}, colon: false},
    button: {autoInsertSpace: false},
    locale: zhCN,
};

export interface HeaderExtraContext {
    activeTopMenu: { key: string; label: string } | null;
    loginInfo: { id: string; name: string; account: string };
}

/** 将 App 上下文的 message 实例注入框架，使提示跟随 ConfigProvider 主题（含暗色） */
function MessageBridge() {
    const {message} = App.useApp();
    React.useEffect(() => {
        setMessageApi(message);
        return () => setMessageApi();
    }, [message]);
    return null;
}

interface LayoutsProps {
    headerExtra?: (context: HeaderExtraContext) => React.ReactNode;
    showOrgSwitcher?: (context: HeaderExtraContext) => boolean;
    /** 主题颜色覆盖，不传使用框架默认主题 */
    colors?: Partial<ThemeColors>;
}

export class Layouts extends React.Component<LayoutsProps> {
    state = {
        location: history.location,
        siteInfoLoaded: false,
        loginChecked: false,
        loginExpiredVisible: false,
        themeMode: getThemeMode(),
        themeVersion: 0,
    };

    unlisten: (() => void) | null = null;
    unsubscribeLoginExpired: (() => void) | null = null;
    unsubscribeLogoutSuccess: (() => void) | null = null;
    unsubscribeTheme: (() => void) | null = null;

    constructor(props: LayoutsProps) {
        super(props);
        setThemeColors(props.colors);
    }

    onLocationChange = ({location}: { location: typeof history.location }) => {
        this.setState({location});
    };

    getPageType(pathname): 'public' | 'standalone' | 'protected' {
        if (pathname === '/' || pathname === '/index') return 'protected';
        if (pathname.startsWith('/public/')) return 'public';
        if (pathname.startsWith('/standalone/')) return 'standalone';
        return 'protected';
    }

    loadData() {
        const {siteInfoLoaded, loginChecked} = this.state;
        const pageType = this.getPageType(this.state.location.pathname);

        if (!siteInfoLoaded) {
            this.loadSiteInfo();
        }

        if ((pageType === 'protected' || pageType === 'standalone') && !loginChecked) {
            this.loadLoginInfo();
        }
    }

    loadSiteInfo() {
        HttpClient.get("/admin/public/site-info", null, {toastError: false}).then(data => {
            GlobalData.setSiteInfo(data.data);
            this.setState({siteInfoLoaded: true});
        }).catch(() => {
            console.error('[Layout] 加载站点信息失败');
        });
    }

    loadLoginInfo() {
        HttpClient.get('/admin/public/login-info', null, {toastError: false}).then(data => {
            GlobalData.setDictInfo(data.data.dictInfo);
            GlobalData.setLoginInfo(data.data.loginInfo);
            GlobalData.setSiteArticles(data.data.siteArticles);

            if (data.data.needUpdatePwd) {
                this.setState({loginChecked: true});
                history.push('/standalone/forceUpdatePwd');
                return;
            }

            this.setState({loginChecked: true});
        }).catch(() => {
            console.error('[Layout] 初始化应用失败');
            PageUtils.redirectToLogin();
        });
    }

    componentDidMount() {
        this.unlisten = history.listen(this.onLocationChange);
        this.unsubscribeLoginExpired = EventBus.on('loginExpired', () => {
            if (!this.state.loginExpiredVisible) {
                this.setState({loginExpiredVisible: true});
            }
        });
        this.unsubscribeLogoutSuccess = EventBus.on('logoutSuccess', () => {
            this.setState({loginChecked: false});
        });
        this.unsubscribeTheme = EventBus.on('themeChange', () => {
            this.setState(prevState => ({
                themeMode: getThemeMode(),
                themeVersion: prevState.themeVersion + 1,
            }));
        });
        this.loadData();
    }

    componentDidUpdate(prevProps: LayoutsProps, prevState: typeof this.state) {
        if (this.state.location !== prevState.location) {
            this.loadData();
        }
    }

    componentWillUnmount() {
        if (this.unlisten) {
            this.unlisten();
        }
        if (this.unsubscribeLoginExpired) {
            this.unsubscribeLoginExpired();
        }
        if (this.unsubscribeLogoutSuccess) {
            this.unsubscribeLogoutSuccess();
        }
        if (this.unsubscribeTheme) {
            this.unsubscribeTheme();
        }
    }

    render() {
        const {location, siteInfoLoaded, loginChecked} = this.state;
        const {pathname, search} = location;
        const ready = siteInfoLoaded && loginChecked;
        const pageType = this.getPageType(pathname);
        const showPageFrame = pageType === 'public' || (pageType === 'standalone' && ready);

        return (
            <ErrorBoundary minimal>
                <ConfigProvider {...baseConfigProps} theme={getThemeConfig()}>
                    <App component={false}>
                        <MessageBridge/>
                        {this.renderContent(showPageFrame, ready, pathname, search)}
                        {this.renderLoginExpiredModal()}
                    </App>
                </ConfigProvider>
            </ErrorBoundary>
        );
    }

    renderContent(showPageFrame: boolean, ready: boolean, pathname: string, search: string) {
        if (showPageFrame) {
            return <PageFrame url={pathname + search}/>;
        }
        if (ready) {
            return <AdminLayout headerExtra={this.props.headerExtra} showOrgSwitcher={this.props.showOrgSwitcher} loginInfo={GlobalData.getLoginInfo()}/>;
        }
        return <PageLoading messages={[
            !this.state.siteInfoLoaded && '加载站点信息...',
            !this.state.loginChecked && '检查登录中...',
        ].filter(Boolean)}/>;
    }

    renderLoginExpiredModal() {
        return (
            <Modal open={this.state.loginExpiredVisible} title="确认操作" okText="确定"
                   onCancel={() => this.setState({loginExpiredVisible: false})}
                   onOk={() => {
                       this.setState({loginExpiredVisible: false, loginChecked: false});
                       PageUtils.redirectToLogin();
                   }}>
                登录已过期，请重新登录
            </Modal>
        );
    }
}

export default Layouts;

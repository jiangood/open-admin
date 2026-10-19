import React from "react";
import {App, ConfigProvider, Modal} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {ErrorBoundary, GlobalData, HttpClient, LOGIN_EXPIRED_REASON_CONCURRENT_LOGIN, PageFrame, PageLoading, PageUtils, history, getThemeConfig, getThemeMode, setThemeColors, setMessageApi, EventBus} from "../framework";
import type {LoginExpiredPayload, ThemeColors} from "../framework";

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

const DEFAULT_LOGIN_EXPIRED_MESSAGE = '登录已过期，请重新登录';

export class Layouts extends React.Component<LayoutsProps> {
    state = {
        location: history.location,
        siteInfoLoaded: false,
        loginChecked: false,
        loginExpiredVisible: false,
        loginExpiredMessage: DEFAULT_LOGIN_EXPIRED_MESSAGE,
        themeMode: getThemeMode(),
        themeVersion: 0,
    };

    unlisten: (() => void) | null = null;
    unsubscribeLoginExpired: (() => void) | null = null;
    unsubscribeLogoutSuccess: (() => void) | null = null;
    unsubscribeTheme: (() => void) | null = null;
    /** 本次登录失效弹窗的原因。同步字段（非 state）：同一次失效会并发上报多个 401，setState 异步可能读到旧值 */
    private loginExpiredReason: string = '';

    constructor(props: LayoutsProps) {
        super(props);
        setThemeColors(props.colors);
    }

    onLocationChange = ({location}: { location: typeof history.location }) => {
        this.setState({location});
    };

    /**
     * 处理登录失效事件并从后端载荷解析弹窗文案。
     * 一次登录失效会并发触发多个请求（各自返回 401、各自上报），已经拿到更具体的原因（并发被顶下线）时不再降级，
     * 避免后到的通用 401 覆盖掉「已在别处登录」文案
     */
    onLoginExpired = (payload?: LoginExpiredPayload) => {
        const concurrent = payload?.reason === LOGIN_EXPIRED_REASON_CONCURRENT_LOGIN;
        if (this.loginExpiredReason === LOGIN_EXPIRED_REASON_CONCURRENT_LOGIN && !concurrent) {
            this.setState({loginExpiredVisible: true});
            return;
        }
        this.loginExpiredReason = concurrent ? LOGIN_EXPIRED_REASON_CONCURRENT_LOGIN : '';
        this.setState({
            loginExpiredVisible: true,
            loginExpiredMessage: this.resolveLoginExpiredMessage(payload),
        });
    };

    closeLoginExpiredModal = () => {
        this.loginExpiredReason = '';
        this.setState({loginExpiredVisible: false});
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
        this.unsubscribeLoginExpired = EventBus.on('loginExpired', this.onLoginExpired);
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

    /**
     * 解析登录失效弹窗文案：
     * - 并发被顶下线：优先用后端随 401 下发的文案（sys.concurrent-login-message）
     * - 其它（空闲超时/未登录）：用站点信息里的文案（sys.login-expired-message）
     */
    resolveLoginExpiredMessage(payload?: LoginExpiredPayload): string {
        if (payload?.reason === LOGIN_EXPIRED_REASON_CONCURRENT_LOGIN && payload.message) {
            return payload.message;
        }
        return GlobalData.getSiteInfo().loginExpiredMessage || payload?.message || DEFAULT_LOGIN_EXPIRED_MESSAGE;
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
                   onCancel={this.closeLoginExpiredModal}
                   onOk={() => {
                       this.closeLoginExpiredModal();
                       this.setState({loginChecked: false});
                       PageUtils.redirectToLogin();
                   }}>
                {this.state.loginExpiredMessage}
            </Modal>
        );
    }
}

export default Layouts;

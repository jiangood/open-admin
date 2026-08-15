import React from "react";
import {ConfigProvider, Modal} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {history, PageFrame} from "../framework";

import AdminLayout from "./admin"
import {HttpClient, PageLoading, PageUtils, GlobalData, getThemeConfig, setThemeColors, EventBus} from "../framework";
import {ErrorBoundary} from "../framework";
import type {ThemeColors} from "../framework";

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
    };

    unlisten: (() => void) | null = null;
    unsubscribeLoginExpired: (() => void) | null = null;
    unsubscribeLogoutSuccess: (() => void) | null = null;

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
        HttpClient.get("/admin/public/site-info", null, data => {
            GlobalData.setSiteInfo(data);
            this.setState({siteInfoLoaded: true});
        }, () => {
            console.error('[Layout] 加载站点信息失败');
        });
    }

    loadLoginInfo() {
        HttpClient.get('/admin/public/login-info', null, data => {
            GlobalData.setDictInfo(data.dictInfo);
            GlobalData.setLoginInfo(data.loginInfo);
            GlobalData.setSiteArticles(data.siteArticles);

            if (data.needUpdatePwd) {
                this.setState({loginChecked: true});
                history.push('/standalone/forceUpdatePwd');
                return;
            }

            this.setState({loginChecked: true});
        }, () => {
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
                    {showPageFrame ? <PageFrame url={pathname + search}/> : ready ? <AdminLayout headerExtra={this.props.headerExtra} showOrgSwitcher={this.props.showOrgSwitcher} loginInfo={GlobalData.getLoginInfo()}/> : (
                        <PageLoading messages={[
                            !siteInfoLoaded && '加载站点信息...',
                            !loginChecked && '检查登录中...',
                        ].filter(Boolean)}/>
                    )}
                    {this.renderLoginExpiredModal()}
                </ConfigProvider>
            </ErrorBoundary>
        );
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

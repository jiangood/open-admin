import React from "react";
import {ConfigProvider} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {Outlet, withRouter} from "umi";

import AdminLayout from "./admin"
import {
    ArrayUtils,
    HttpUtils,
    MessageHolder,
    PageLoading,
    PageUtils, StringUtils,
    SysUtils,
    ThemeUtils,
} from "../framework";
import {Logger} from "../framework/utils/Logger";

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

// 不需要登录的页面
const SIMPLE_URLS = ['/login', '/test']

function checkIsSimplePage(pathname) {
    if(pathname === '/' || pathname === '/index'){
        return false;
    }
    // 测试页面
    if(pathname.startsWith("/test/")){
        return true
    }
    return ArrayUtils.contains(SIMPLE_URLS, pathname) ;
}

function checkIsPurePage(pathname){
    if(pathname.startsWith("/test/")){
        return true
    }
    return false
}

class _Layouts extends React.Component {

    log = Logger.getLogger('Layouts')

    state = {
        messageHolderInit:false,
        siteInfoLoading: true,
        loginInfoFinish: false
    }

    onMessageHolderFinish = () => {
        this.loadSiteInfo()
        this.setState({messageHolderInit:true});
    }


    loadSiteInfo = () => {
        HttpUtils.get("/admin/public/site-info").then(rs => {
            SysUtils.setSiteInfo(rs)
            this.setState({siteInfoLoading: false})
            this.loadLoginInfo()
        })
    };

    componentDidUpdate(prevProps, prevState, snapshot) {
        const pre = prevProps.location.pathname
        const cur = this.props.location.pathname
        if (pre !== cur) {
            this.loadLoginInfo()
        }
    }



    loadLoginInfo = () => {
        const {pathname} = this.props.location;
        if (checkIsPurePage(pathname) || checkIsSimplePage(pathname) || this.state.loginInfoFinish) {
            return;
        }

        HttpUtils.get('/admin/public/check-login')
            .then(rs => {
                const {needUpdatePwd, dictInfo, loginInfo} = rs
                SysUtils.setDictInfo(dictInfo)
                SysUtils.setLoginInfo(loginInfo)
                if (!needUpdatePwd) {
                    this.setState({loginInfoFinish: true});
                    return;
                }

                if (needUpdatePwd) {
                    PageUtils.open('/userCenter/ChangePassword', '修改密码')
                    return;
                }
            })
            .catch(async () => {
                PageUtils.redirectToLogin()
            })
    }

    render() {
        return <ConfigProvider
            input={{autoComplete: 'off'}}
            form={{
                validateMessages: {
                    required: '必填项'
                }, colon: false
            }}
            button={{
                autoInsertSpace: false
            }}
            locale={zhCN}
            theme={{
                token: {
                    colorPrimary: ThemeUtils.getColor("primary-color"),
                    colorSuccess: ThemeUtils.getColor("success-color"),
                    colorWarning: ThemeUtils.getColor("warning-color"),
                    colorError: ThemeUtils.getColor("error-color"),
                    borderRadius: 4,
                },
                components: {
                    Menu: {
                        darkItemBg: ThemeUtils.getColor("primary-color"),
                        darkPopupBg: ThemeUtils.getColor("primary-color"),
                        darkItemSelectedBg: ThemeUtils.getColor("primary-color-click"),
                        darkItemHoverBg: ThemeUtils.getColor("primary-color-hover"),
                        darkSubMenuItemBg: ThemeUtils.getColor("primary-color")
                    },
                    Layout: {
                        siderBg: ThemeUtils.getColor("primary-color"),
                        triggerBg: ThemeUtils.getColor("primary-color-click"),
                        headerBg: 'white',
                        triggerHeight: 32
                    }
                }
            }}>

            <MessageHolder onFinish={this.onMessageHolderFinish} />
            {this.renderContent()}
        </ConfigProvider>
    }


    renderContent = () => {
        const {pathname} = this.props.location;

        if(checkIsPurePage(pathname)){
            return <Outlet/>
        }

        if(!this.state.messageHolderInit) {
            this.log.info('加载message holder...')
            return <PageLoading message='加载消息组件...'/>
        }

        const {params = {}} = this.props.location;
        if (checkIsSimplePage(pathname) || params.hasOwnProperty('_noLayout')) {
            return <Outlet/>
        }



        if (this.state.siteInfoLoading) {
            return <PageLoading message='加载站点信息...'/>
        }
        


        if (!this.state.loginInfoFinish) {
            return <PageLoading message='加载登录信息...'/>
        }

        return <AdminLayout path={this.state.path} logo={this.props.logo}/>
    };
}


export const Layouts = withRouter(_Layouts);
export default Layouts
export * from './PageRender'

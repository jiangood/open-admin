import AdminLayout from "./admin"
import React from "react";

import {ConfigProvider} from "antd";

import {Outlet, withRouter} from "umi";
import zhCN from 'antd/locale/zh_CN';
import {ArrUtils, HttpUtils, PageLoading, PageUtils, SysUtils, ThemeUtils,} from "../framework";
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

// 不需要登录的页面
const SIMPLE_URLS = ['/login', '/test']

class _Layouts extends React.Component {


    state = {
        siteInfoLoading: true,
        loginInfoFinish: false
    }


    componentDidMount() {
        HttpUtils.get("/admin/public/site-info").then(rs => {
            SysUtils.setSiteInfo(rs)
            this.setState({siteInfoLoading: false})

            this.loadLoginInfo()
        })
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const pre = prevProps.location.pathname
        const cur = this.props.location.pathname

        if (pre !== cur) {
            this.loadLoginInfo()
        }
    }

    isSimplePage() {
        let {pathname} = this.props.location;
        return ArrUtils.contains(SIMPLE_URLS, pathname)
    }

    loadLoginInfo = () => {
        if (this.isSimplePage() || this.state.loginInfoFinish) {
            return;
        }

        HttpUtils.get('/admin/public/checkLogin')
            .then(rs => {
                const {needUpdatePwd, dictMap, loginInfo} = rs
                SysUtils.setDictInfo(dictMap)
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
                    },


                }
            }}>

            {this.renderContent()}

        </ConfigProvider>
    }

    renderContent = () => {
        if (this.state.siteInfoLoading) {
            return <PageLoading message='加载站点信息...'/>
        }
        let {params = {}} = this.props.location;
        console.log('layout: params', params)
        let simple = this.isSimplePage();
        if (simple || params.hasOwnProperty('_noLayout')) {
            return <Outlet/>
        }

        if (!this.state.loginInfoFinish) {
            return <PageLoading message='加载登录信息...'/>
        }


        return <AdminLayout path={this.state.path} logo={this.props.logo}/>
    };

    renderMessages = () => {

    };

}

export const Layouts = withRouter(_Layouts);
export default Layouts
export * from './PageRender'

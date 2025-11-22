import AdminLayout from "./admin"
import React from "react";

import {ConfigProvider} from "antd";

import {history, Outlet, withRouter} from "umi";
import zhCN from 'antd/locale/zh_CN';
import {ArrUtil, HttpUtil, MsgBoxComponent, PageLoading, PageUtil, SysUtil, theme} from "../framework";
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
        HttpUtil.get("/admin/public/site-info").then(rs => {
            SysUtil.setSiteInfo(rs)
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
        return ArrUtil.contains(SIMPLE_URLS, pathname)
    }

    loadLoginInfo = () => {
        if (this.isSimplePage() || this.state.loginInfoFinish) {
            return;
        }

        HttpUtil.get('admin/public/checkLogin')
            .then(rs => {
                const {needUpdatePwd, dictTree, loginInfo} = rs
                SysUtil.setDictInfo(dictTree)
                SysUtil.setLoginInfo(loginInfo)
                if (!needUpdatePwd) {
                    this.setState({loginInfoFinish: true});
                    return;
                }

                if (needUpdatePwd) {
                    PageUtil.open('/userCenter/ChangePassword', '修改密码')
                    return;
                }
            })
            .catch(async () => {
                PageUtil.redirectToLogin()
            })
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
                    colorPrimary: theme["primary-color"],
                    colorSuccess: theme["success-color"],
                    colorWarning: theme["warning-color"],
                    colorError: theme["error-color"],
                    borderRadius: 4,

                },
                components: {
                    Menu: {
                        darkItemBg: theme["primary-color"],
                        darkPopupBg: theme["primary-color"],
                        darkItemSelectedBg: theme["primary-color-click"],
                        darkItemHoverBg: theme["primary-color-hover"],
                        darkSubMenuItemBg: theme["primary-color"]
                    },
                    Layout: {
                        siderBg: theme["primary-color"],
                        triggerBg: theme["primary-color-click"],
                        headerBg: 'white',
                        triggerHeight: 32
                    },
                }
            }}>

            {this.renderContent()}

            {this.renderGlobalComponent()}
        </ConfigProvider>
    }


    renderGlobalComponent() {
        console.log('渲染全局组件')
        return <div className='global-component'>
            <MsgBoxComponent/>
        </div>
    }
}

export const Layouts = withRouter(_Layouts);
export default Layouts
export * from './PageRender'

import React, {useState, useEffect, useRef} from "react";
import {App, ConfigProvider} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {Outlet, useLocation} from "umi";

import AdminLayout from "./admin"
import {HttpUtils, PageLoading, PageUtils, SysUtils, ThemeUtils} from "../framework";

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

// 不需要登录和布局的页面
const PUBLIC_PAGES = ['/login', '/test']

function isPublicPage(pathname, search) {
    if (pathname === '/' || pathname === '/index') return false;
    if (pathname.startsWith("/test/")) return true;
    if (PUBLIC_PAGES.includes(pathname)) return true;
    if (search && new URLSearchParams(search).has('_noLayout')) return true;
    return false;
}

function getConfigProps() {
    return {
        input: {autoComplete: 'off'},
        form: {validateMessages: {required: '必填项'}, colon: false},
        button: {autoInsertSpace: false},
        locale: zhCN,
        theme: {
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
                    darkSubMenuItemBg: ThemeUtils.getColor("primary-color"),
                },
                Layout: {
                    siderBg: ThemeUtils.getColor("primary-color"),
                    triggerBg: ThemeUtils.getColor("primary-color-click"),
                    headerBg: 'white',
                    triggerHeight: 32,
                },
            },
        },
    };
}

function initApp() {
    return Promise.all([
        HttpUtils.get("/admin/public/site-info"),
        HttpUtils.get('/admin/public/check-login'),
    ]).then(([siteInfo, loginRs]) => {
        SysUtils.setSiteInfo(siteInfo);
        const {needUpdatePwd, dictInfo, loginInfo} = loginRs;
        SysUtils.setDictInfo(dictInfo);
        SysUtils.setLoginInfo(loginInfo);
        if (needUpdatePwd) {
            PageUtils.open('/userCenter/ChangePassword', '修改密码');
            return false;
        }
        return true;
    }).catch((e) => {
        console.error('[Layout] 初始化应用失败:', e);
        PageUtils.redirectToLogin();
        return false;
    });
}

export function Layouts() {
    const {pathname, search} = useLocation();
    const [ready, setReady] = useState(false);
    const loadedRef = useRef(false);

    useEffect(() => {
        if (isPublicPage(pathname)) return;
        if (loadedRef.current) return;
        loadedRef.current = true;
        initApp().then(ok => { if (ok) setReady(true); });
    }, [pathname]);

    if (isPublicPage(pathname, search)) {
        return <ConfigProvider {...getConfigProps()}><App><Outlet/></App></ConfigProvider>;
    }

    if (!ready) {
        return <ConfigProvider {...getConfigProps()}><App><PageLoading message='加载中...'/></App></ConfigProvider>;
    }

    return <ConfigProvider {...getConfigProps()}><App><AdminLayout/></App></ConfigProvider>;
}

export default Layouts;
export * from './PageRender'
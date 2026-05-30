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

const configProps = {
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

function AppWrapper({children}) {
    return <ConfigProvider {...configProps}><App>{children}</App></ConfigProvider>;
}

function isPublicPage(pathname, search) {
    if (pathname === '/' || pathname === '/index') return false;

    const raw = typeof OPEN_ADMIN_PUBLIC_PAGES !== 'undefined' && OPEN_ADMIN_PUBLIC_PAGES;
    const pages = raw ? raw.split(',').map(s => s.trim()) : ['/login', '/test'];

    for (const pattern of pages) {
        if (pattern.endsWith('/**')) {
            if (pathname.startsWith(pattern.slice(0, -3))) return true;
        } else if (pathname === pattern) {
            return true;
        }
    }

    if (search && new URLSearchParams(search).has('_noLayout')) return true;
    return false;
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
    }).catch(() => {
        PageUtils.redirectToLogin();
        return false;
    });
}

export function Layouts() {
    const {pathname, search} = useLocation();
    const [ready, setReady] = useState(false);
    const loadedRef = useRef(false);

    useEffect(() => {
        if (isPublicPage(pathname, search)) return;
        if (loadedRef.current) return;
        loadedRef.current = true;
        initApp().then(ok => ok && setReady(true));
    }, [pathname]);

    if (isPublicPage(pathname, search)) {
        return <AppWrapper><Outlet/></AppWrapper>;
    }

    return (
        <AppWrapper>
            {ready ? <AdminLayout/> : <PageLoading message='加载中...'/>}
        </AppWrapper>
    );
}

export default Layouts;
export * from './PageRender'
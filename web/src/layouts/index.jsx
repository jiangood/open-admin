import React, {useState, useEffect, useRef} from "react";
import {App, ConfigProvider} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {Outlet, useLocation} from "umi";

import AdminLayout from "./admin"
import {
    ArrayUtils,
    HttpUtils,
    PageLoading,
    PageUtils,
    SysUtils,
    ThemeUtils,
} from "../framework";

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

const SIMPLE_URLS = ['/login', '/test']

function checkIsSimplePage(pathname) {
    if (pathname === '/' || pathname === '/index') return false;
    if (pathname.startsWith("/test/")) return true;
    return ArrayUtils.contains(SIMPLE_URLS, pathname);
}

function checkIsPurePage(pathname) {
    return pathname.startsWith("/test/");
}

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
                darkSubMenuItemBg: ThemeUtils.getColor("primary-color")
            },
            Layout: {
                siderBg: ThemeUtils.getColor("primary-color"),
                triggerBg: ThemeUtils.getColor("primary-color-click"),
                headerBg: 'white',
                triggerHeight: 32
            }
        }
    }
};

export function Layouts() {
    const location = useLocation();
    const {pathname, search} = location;
    const noLayout = search && new URLSearchParams(search).has('_noLayout');

    const [siteInfoLoading, setSiteInfoLoading] = useState(true);
    const [loginInfoFinish, setLoginInfoFinish] = useState(false);
    const loginInfoFinishRef = useRef(false);
    const siteInfoLoadedRef = useRef(false);

    useEffect(() => {
        if (checkIsPurePage(pathname) || checkIsSimplePage(pathname)) return;
        if (siteInfoLoadedRef.current) return;
        siteInfoLoadedRef.current = true;
        loadSiteInfo();
    }, [pathname]);

    useEffect(() => {
        if (loginInfoFinish) return;
        if (checkIsPurePage(pathname) || checkIsSimplePage(pathname)) return;
        loadLoginInfo();
    }, [pathname]);

    const loadLoginInfo = () => {
        if (checkIsPurePage(pathname) || checkIsSimplePage(pathname) || loginInfoFinishRef.current) return;

        HttpUtils.get('/admin/public/check-login')
            .then(rs => {
                const {needUpdatePwd, dictInfo, loginInfo} = rs;
                SysUtils.setDictInfo(dictInfo);
                SysUtils.setLoginInfo(loginInfo);
                if (!needUpdatePwd) {
                    setLoginInfoFinish(true);
                    loginInfoFinishRef.current = true;
                    return;
                }
                PageUtils.open('/userCenter/ChangePassword', '修改密码');
            })
            .catch(async () => {
                PageUtils.redirectToLogin();
            });
    };

    const loadSiteInfo = () => {
        HttpUtils.get("/admin/public/site-info").then(rs => {
            SysUtils.setSiteInfo(rs);
            setSiteInfoLoading(false);
            loadLoginInfo();
        });
    };

    if (checkIsPurePage(pathname)) {
        return <ConfigProvider {...configProps}><App><Outlet/></App></ConfigProvider>;
    }

    if (checkIsSimplePage(pathname) || noLayout) {
        return <ConfigProvider {...configProps}><App><Outlet/></App></ConfigProvider>;
    }

    if (siteInfoLoading) {
        return <ConfigProvider {...configProps}><App><PageLoading message='加载站点信息...'/></App></ConfigProvider>;
    }

    if (!loginInfoFinish) {
        return <ConfigProvider {...configProps}><App><PageLoading message='加载登录信息...'/></App></ConfigProvider>;
    }

    return <ConfigProvider {...configProps}><App><AdminLayout/></App></ConfigProvider>;
}

export default Layouts;
export * from './PageRender'
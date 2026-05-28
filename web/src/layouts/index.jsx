import React, {useState, useEffect, useRef} from "react";
import {App, ConfigProvider} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {Outlet, history} from "umi";

import AdminLayout from "./admin"
import {HttpUtils, PageLoading, PageUtils, SysUtils, ThemeUtils} from "../framework";

import '../style/global.less'
import './index.less'

dayjs.locale('zh-cn');

const SIMPLE_URLS = ['/login', '/test']

function isPublicPage(pathname, search) {
    if (pathname === '/' || pathname === '/index') return false;
    if (pathname.startsWith("/test/")) return true;
    if (SIMPLE_URLS.includes(pathname)) return true;
    if (search && new URLSearchParams(search).has('_noLayout')) return true;
    return false;
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
    const [ready, setReady] = useState(false);
    const loadedRef = useRef(false);

    useEffect(() => {
        // Load siteInfo and checkLogin in parallel
        const init = () => {
            const {pathname} = history.location;
            if (isPublicPage(pathname)) return;

            loadedRef.current = true;
            Promise.all([
                HttpUtils.get("/admin/public/site-info"),
                HttpUtils.get('/admin/public/check-login'),
            ]).then(([siteInfo, loginRs]) => {
                SysUtils.setSiteInfo(siteInfo);
                const {needUpdatePwd, dictInfo, loginInfo} = loginRs;
                SysUtils.setDictInfo(dictInfo);
                SysUtils.setLoginInfo(loginInfo);
                if (needUpdatePwd) {
                    PageUtils.open('/userCenter/ChangePassword', '修改密码');
                    return;
                }
                setReady(true);
            }).catch(() => {
                PageUtils.redirectToLogin();
            });
        };

        init();

        // Re-check login on route changes (e.g. after 401 redirect back)
        const unlisten = history.listen(({location}) => {
            if (isPublicPage(location.pathname)) return;
            if (!loadedRef.current) {
                init();
            }
        });

        return unlisten;
    }, []);

    const {pathname, search} = history.location;

    if (isPublicPage(pathname, search)) {
        return <ConfigProvider {...configProps}><App><Outlet/></App></ConfigProvider>;
    }

    if (!ready) {
        return <ConfigProvider {...configProps}><App><PageLoading message='加载中...'/></App></ConfigProvider>;
    }

    return <ConfigProvider {...configProps}><App><AdminLayout/></App></ConfigProvider>;
}

export default Layouts;
export * from './PageRender'
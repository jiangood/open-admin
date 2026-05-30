import {useState, useEffect, useRef} from "react";
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

const PUBLIC_PAGES = (() => {
    const raw = typeof OPEN_ADMIN_PUBLIC_PAGES !== 'undefined' && OPEN_ADMIN_PUBLIC_PAGES;
    return raw ? raw.split(',').map(s => s.trim()) : ['/login', '/test'];
})();

function isPublicPage(pathname, search) {
    if (pathname === '/' || pathname === '/index') return false;

    for (const pattern of PUBLIC_PAGES) {
        if (pattern.endsWith('/**')) {
            if (pathname.startsWith(pattern.slice(0, -3))) return true;
        } else if (pathname === pattern) {
            return true;
        }
    }

    if (new URLSearchParams(search).has('_noLayout')) return true;
    return false;
}

async function initApp() {
    try {
        const [siteInfo, loginRs] = await Promise.all([
            HttpUtils.get("/admin/public/site-info"),
            HttpUtils.get('/admin/public/check-login'),
        ]);
        SysUtils.setSiteInfo(siteInfo);
        const {needUpdatePwd, dictInfo, loginInfo} = loginRs;
        SysUtils.setDictInfo(dictInfo);
        SysUtils.setLoginInfo(loginInfo);
        if (needUpdatePwd) {
            PageUtils.open('/userCenter/ChangePassword', '修改密码');
            return false;
        }
        return true;
    } catch (e) {
        console.error('[Layout] 初始化应用失败:', e);
        PageUtils.redirectToLogin();
        return false;
    }
}

export function Layouts() {
    const {pathname, search} = useLocation();
    const [ready, setReady] = useState(false);
    const initPromise = useRef(null);

    useEffect(() => {
        if (isPublicPage(pathname, search)) {
            initPromise.current = null;
            return;
        }
        if (initPromise.current) return;
        initPromise.current = initApp().then(ok => ok && setReady(true));
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
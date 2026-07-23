import {useState, useEffect} from "react";
import {App, ConfigProvider} from "antd";
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {Outlet, useLocation, history} from "umi";

import AdminLayout from "./admin"
import {HttpUtils, PageLoading, PageUtils, GlobalData, ThemeUtils, DownloadModal} from "../framework";
import {ErrorBoundary} from "../framework";

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
            Layout: {
                headerBg: 'white',
                triggerHeight: 32,
            },
        },
    },
};

function AppWrapper({children}) {
    return <ConfigProvider {...configProps}><App><DownloadModal />{children}</App></ConfigProvider>;
}

const PUBLIC_PAGES = (() => {
    const raw = typeof OPEN_ADMIN_PUBLIC_PAGES !== 'undefined' && OPEN_ADMIN_PUBLIC_PAGES;
    return raw ? raw.split(',').map(s => s.trim()) : ['/login', '/test', '/forceUpdatePwd'];
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

export function Layouts() {
    const {pathname, search} = useLocation();
    const [siteInfoLoaded, setSiteInfoLoaded] = useState(false);
    const [loginChecked, setLoginChecked] = useState(false);

    const ready = siteInfoLoaded && loginChecked;
    const isPublic = isPublicPage(pathname, search);

    useEffect(() => {
        if (isPublic) {
            setSiteInfoLoaded(false);
            setLoginChecked(false);
            return;
        }
        if (ready) return;

        Promise.all([
            HttpUtils.get("/admin/public/site-info").then(data => {
                GlobalData.setSiteInfo(data);
                setSiteInfoLoaded(true);
            }),
            HttpUtils.get('/admin/public/check-login').then(data => {
                GlobalData.setDictInfo(data.dictInfo);
                GlobalData.setLoginInfo(data.loginInfo);

                if (data.needUpdatePwd && pathname !== '/forceUpdatePwd') {
                    history.push('/forceUpdatePwd');
                    return;
                }

                setLoginChecked(true);
            }),
        ]).catch(() => {
            console.error('[Layout] 初始化应用失败');
            PageUtils.redirectToLogin();
        });
    }, [pathname]);

    if (isPublic) return <ErrorBoundary minimal><AppWrapper><Outlet/></AppWrapper></ErrorBoundary>;

    return (
        <ErrorBoundary minimal>
            <AppWrapper>
                {ready ? <AdminLayout/> : (
                    <PageLoading messages={[
                        !siteInfoLoaded && '加载站点信息...',
                        !loginChecked && '检查登录中...',
                    ].filter(Boolean)}/>
                )}
            </AppWrapper>
        </ErrorBoundary>
    );
}

export default Layouts;
export * from './PageRender'
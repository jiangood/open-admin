import React from 'react';
import {ConfigProvider, Drawer, Layout, Menu, Skeleton, Watermark} from 'antd';
import {CloseOutlined, MenuOutlined} from '@ant-design/icons';

import "./index.less"
import {HttpClient, NamedIcon, PageUtils, GlobalData, TreeUtils, history, Link, ARTICLE_HEADER_LEFT, OrgSwitcher, getSiderThemeConfig, getLayoutMode, getMenuMode, EventBus} from "../../framework";

import { HeaderRight } from "./HeaderRight";
import { TabLayout } from "./TabLayout";
import { SinglePageLayout } from "./SinglePageLayout";
import { classifyMenus, findActiveTopMenu, findAncestorKeys, containsMenuKey, expandForMenu } from "./menuUtils";
const {Header, Sider, Content} = Layout;
/**
 * 带菜单的布局，主要处理布局宇框架结构
 */
export default class AdminLayout extends React.Component {

    state = {
        loginInfo: {},

        menuTree: [],
        menuMap: {},
        pathMenuMap: {},
        menuLoading:true,

        shortcuts: [],

        currentMenuKey: null,

        // 左侧菜单默认展开的目录；侧边模式下基于完整菜单树，顶部模式下基于当前一级菜单的子菜单
        openKeys: [],

        // 顶部菜单模式：一级菜单、当前一级菜单下的子菜单
        topMenus: [],
        sideMenus: [],
        activeTopMenuKey: null,

        siteInfo: {},

        headerLeftArticles: [],

        layoutMode: getLayoutMode(),

        // 菜单模式：side 侧边 / top 顶部；移动端固定使用汉堡抽屉
        menuMode: getMenuMode(),

        // 手机端：左侧栏完全收起，改由顶栏汉堡按钮 + 抽屉展示菜单
        isMobile: typeof window !== 'undefined' && typeof window.matchMedia === 'function'
            ? window.matchMedia('(max-width: 768px)').matches
            : false,
        mobileMenuOpen: false,
    }


    unsubscribeLayout = null;
    unsubscribeMenuMode = null;
    unsubscribeSiteInfo = null;
    unsubscribeShortcuts = null;
    unlistenRoute = null;
    mobileMediaQuery = null;
    handleMobileMediaChange = null;

    componentDidMount() {
        // 窄屏监听：进入/退出手机布局时切换左侧栏与汉堡抽屉
        if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
            this.mobileMediaQuery = window.matchMedia('(max-width: 768px)');
            this.handleMobileMediaChange = e => {
                this.setState({
                    isMobile: e.matches,
                    // 页签模式在窄屏固定为单页，窗口缩放时同步刷新
                    layoutMode: getLayoutMode(),
                    // 退回桌面宽度时关闭抽屉，避免残留遮罩
                    mobileMenuOpen: e.matches ? this.state.mobileMenuOpen : false,
                }, () => this.syncMenuSelection());
            };
            this.mobileMediaQuery.addEventListener('change', this.handleMobileMediaChange);
        }

        const siteInfo = GlobalData.getSiteInfo();
        const loginInfo = GlobalData.getLoginInfo()
        this.setState({siteInfo, loginInfo})

        this.initMenu()
        this.loadShortcuts()

        // 浏览器前进/后退、页签切换等路由变化时同步菜单选中与展开状态
        this.unlistenRoute = history.listen(() => this.syncMenuSelection());

        this.unsubscribeShortcuts = EventBus.on('shortcutsChange', this.loadShortcuts)

        const siteArticles = GlobalData.getSiteArticles();
        this.setState({headerLeftArticles: siteArticles[ARTICLE_HEADER_LEFT] || []})

        this.unsubscribeLayout = EventBus.on('layoutChange', () => {
            this.setState({layoutMode: getLayoutMode()});
        })

        this.unsubscribeMenuMode = EventBus.on('menuModeChange', () => {
            this.setState({menuMode: getMenuMode()}, () => this.syncMenuSelection());
        })

        // 系统参数保存后刷新标题、水印等站点信息
        this.unsubscribeSiteInfo = EventBus.on('siteInfoChange', () => {
            this.setState({siteInfo: GlobalData.getSiteInfo()});
        })
    }

    componentWillUnmount() {
        if (this.unsubscribeLayout) {
            this.unsubscribeLayout();
        }
        if (this.unsubscribeMenuMode) {
            this.unsubscribeMenuMode();
        }
        if (this.unsubscribeSiteInfo) {
            this.unsubscribeSiteInfo();
        }
        if (this.unsubscribeShortcuts) {
            this.unsubscribeShortcuts();
        }
        if (this.unlistenRoute) {
            this.unlistenRoute();
        }
        if (this.mobileMediaQuery && this.handleMobileMediaChange) {
            this.mobileMediaQuery.removeEventListener('change', this.handleMobileMediaChange);
        }
    }


    openMobileMenu = () => this.setState({mobileMenuOpen: true});

    closeMobileMenu = () => this.setState({mobileMenuOpen: false});

    loadShortcuts = () => {
        HttpClient.get('admin/user-center/shortcuts', null, {autoShowErrorMessage: false})
            .then(rs => this.setState({shortcuts: rs.data || []}))
            .catch(() => this.setState({shortcuts: []}))
    }


    initMenu = () => {
        this.setState({menuLoading: true})
        HttpClient.get('/admin/menu-info', null, {autoShowErrorMessage: false}).then(info => {
            const {menuTree, pathMenuMap, menuMap} = info.data

            TreeUtils.walk(menuTree, (item) => {
                if (item.type === 'divider') {
                    return
                }
                item.icon = <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 16}}/>
            })

            // 顶部菜单模式的一级菜单：根级目录 + 根级叶子菜单归入的「业务模块」
            const topMenus = classifyMenus(menuTree)

            this.setState({menuTree, menuMap, pathMenuMap, topMenus, menuLoading: false},
                () => this.syncMenuSelection())
        }).catch(err => {
            console.error('加载菜单失败:', err)
            this.setState({menuTree: [], topMenus: [], menuLoading: false})
        })
    }
    actionRef = React.createRef()

    /**
     * 根据当前路由同步菜单选中、展开与顶部菜单状态。
     */
    syncMenuSelection = () => {
        const {menuTree, menuMap, pathMenuMap, topMenus, menuMode, isMobile} = this.state;
        const pathname = PageUtils.currentPathname();
        const menuDef = pathname !== '' && pathname !== '/' ? pathMenuMap[pathname] : null;
        const currentMenuKey = menuDef ? menuDef.id : null;

        if (menuMode === 'top' && !isMobile) {
            // 能匹配到菜单时切到所属一级菜单；匹配不到（首页/个人中心等）保持当前一级菜单
            const matched = findActiveTopMenu(topMenus, menuDef, menuMap);
            const activeTopMenuKey = matched
                ? matched.key
                : (this.state.activeTopMenuKey && topMenus.some(t => t.key === this.state.activeTopMenuKey)
                    ? this.state.activeTopMenuKey
                    : (topMenus.length > 0 ? topMenus[0].key : null));
            const topMenu = topMenus.find(t => t.key === activeTopMenuKey);
            const sideMenus = topMenu ? (topMenu.children || []) : [];
            const inSide = currentMenuKey ? containsMenuKey(sideMenus, currentMenuKey) : false;
            // 同一一级菜单内保留用户已展开的其它目录；切换一级菜单时重置
            const sameTop = this.state.activeTopMenuKey === activeTopMenuKey;

            this.setState({
                activeTopMenuKey,
                sideMenus,
                currentMenuKey: inSide ? currentMenuKey : null,
                openKeys: inSide
                    ? expandForMenu(sideMenus, sameTop ? this.state.openKeys : [], currentMenuKey)
                    : [],
            });
            return;
        }

        // 侧边菜单模式 / 移动端抽屉：渲染完整菜单树
        this.setState({
            currentMenuKey,
            openKeys: currentMenuKey
                ? expandForMenu(menuTree, this.state.openKeys, currentMenuKey)
                : this.state.openKeys,
            activeTopMenuKey: null,
            sideMenus: [],
        });
    }

    onTopMenuClick = ({key}) => {
        const topMenu = this.state.topMenus.find(t => t.key === key);
        if (!topMenu) return;

        const sideMenus = topMenu.children || [];
        const {currentMenuKey} = this.state;
        const inSide = currentMenuKey ? containsMenuKey(sideMenus, currentMenuKey) : false;

        this.setState({
            activeTopMenuKey: key,
            sideMenus,
            // 当前页面不在新一级菜单下时清空选中，避免侧栏出现无高亮的假象
            currentMenuKey: inSide ? currentMenuKey : null,
            openKeys: inSide ? (findAncestorKeys(sideMenus, currentMenuKey) || []) : [],
        });
    }

    onLeftMenuClick = ({key}) => {
        const menu = this.state.menuMap[key];
        // 目录节点没有 path，点击由展开/收起处理
        if (!menu || !menu.path) return;

        const {menuMode, isMobile, menuTree, sideMenus, topMenus, menuMap, openKeys} = this.state;
        const useFullTree = menuMode === 'side' || isMobile;
        const container = useFullTree ? menuTree : sideMenus;

        const next = {
            currentMenuKey: key,
            mobileMenuOpen: false,
            openKeys: expandForMenu(container, openKeys, key),
        };

        if (!useFullTree) {
            const matched = findActiveTopMenu(topMenus, menu, menuMap);
            if (matched && matched.key !== this.state.activeTopMenuKey) {
                next.activeTopMenuKey = matched.key;
                next.sideMenus = matched.children || [];
                next.openKeys = findAncestorKeys(next.sideMenus, key) || [];
            }
        }

        this.setState(next);
        PageUtils.open(menu.path);
    }

    /** 顶部菜单模式下当前激活的一级菜单；侧边/移动端为 null */
    getActiveTopMenu() {
        const {menuMode, isMobile, topMenus, activeTopMenuKey} = this.state;
        if (menuMode !== 'top' || isMobile || !activeTopMenuKey) return null;
        return (topMenus || []).find(t => t.key === activeTopMenuKey) || null;
    }

    render() {
        const {siteInfo, loginInfo} = this.state
        // 侧栏空间有限，优先显示系统简称；未配置时回退完整标题
        const siderTitle = (siteInfo.shortTitle || '').trim() || siteInfo.title || '首页';

        const headerExtraNode = this.props.headerExtra?.({
            activeTopMenu: this.getActiveTopMenu(),
            loginInfo: this.props.loginInfo,
        });
        // 只有真正有内容时才占位，避免无权限角色留下空容器影响顶栏布局
        // （null/undefined/布尔值在 React 中都不渲染，视为没有额外内容）
        const hasHeaderExtra = headerExtraNode !== null && headerExtraNode !== undefined
            && typeof headerExtraNode !== 'boolean';
        const headerClassName = 'header' + (this.props.headerExtraStretch && hasHeaderExtra ? ' has-stretch-extra' : '');

return <Layout className='main-layout'>
             {/* 手机端不渲染左侧栏，改由顶栏汉堡按钮 + 抽屉展示菜单；桌面端保留原侧栏折叠行为 */}
             {!this.state.isMobile && (
                 <ConfigProvider theme={getSiderThemeConfig()}>
                 <Sider id='left-sider'
                        width={180}
                        collapsible
                        breakpoint={'md'}
                 >
                    <div className='sider-header'>
                        <button type="button" className='logo-img'
                                onClick={() => history.push('/')}>
                            <img src="./logo.png" alt={siteInfo.title || '首页'}/>
                        </button>
                        <h3 className='hide-on-mobile'>
                            <Link to="/" title={siteInfo.title || siderTitle}
                                  style={{color: 'var(--oa-sider-text, rgba(255,255,255,0.85))'}}>{siderTitle}</Link>
                        </h3>
                    </div>
                    {this.renderShortcutMenu()}
                    {this.renderLeftMenu()}

                </Sider>
                 </ConfigProvider>
             )}

            <Layout style={{flex: 1, overflow: 'hidden'}}>
                <Header className={headerClassName}>
                    <button type="button" className="item mobile-menu-trigger" aria-label="打开菜单"
                            onClick={this.openMobileMenu}>
                        <MenuOutlined/>
                    </button>
                    {this.renderTopMenu()}
                    {this.state.headerLeftArticles.map(a => (
                        <button key={a.code} type="button" className='item'
                                onClick={() => PageUtils.open('/article/' + a.code)}>{a.title}</button>
                    ))}
                    {this.props.showOrgSwitcher?.({
                        activeTopMenu: this.getActiveTopMenu(),
                        loginInfo: this.props.loginInfo,
                    }) && <OrgSwitcher/>}
                    {hasHeaderExtra && <div className='header-extra'>{headerExtraNode}</div>}
                    <HeaderRight/>
                </Header>

                <Content id='admin-layout-content'>
                    {this.renderCenterContent(loginInfo)}
                </Content>

            </Layout>

            {this.renderMobileDrawer()}
        </Layout>
    }


    /** 顶部菜单模式：顶栏一级菜单。移动端固定抽屉导航，不渲染横向菜单 */
    renderTopMenu = () => {
        const {menuMode, menuLoading, topMenus, activeTopMenuKey, isMobile} = this.state;
        if (isMobile || menuMode !== 'top' || menuLoading) return null;
        if (!topMenus || topMenus.length === 0) return null;

        return (
            <nav className="top-nav">
                {topMenus.map(item => (
                    <button
                        key={item.key}
                        type="button"
                        className={'top-nav-item' + (item.key === activeTopMenuKey ? ' active' : '')}
                        onClick={() => this.onTopMenuClick({key: item.key})}
                    >
                        {item.label}
                    </button>
                ))}
            </nav>
        );
    }


    /** 点击快捷菜单时同步菜单的选中项并展开所属目录 */
    activateMenuForShortcut(shortcut) {
        const {menuMode, isMobile, menuTree, menuMap, topMenus, openKeys} = this.state;

        if (menuMode === 'top' && !isMobile) {
            const matched = findActiveTopMenu(topMenus, menuMap[shortcut.id], menuMap);
            const activeTopMenuKey = matched ? matched.key : this.state.activeTopMenuKey;
            const topMenu = (topMenus || []).find(t => t.key === activeTopMenuKey);
            const sideMenus = topMenu ? (topMenu.children || []) : [];
            this.setState({
                currentMenuKey: shortcut.id,
                activeTopMenuKey: topMenu ? topMenu.key : null,
                sideMenus,
                openKeys: findAncestorKeys(sideMenus, shortcut.id) || [],
                mobileMenuOpen: false,
            });
            return;
        }

        this.setState({
            currentMenuKey: shortcut.id,
            openKeys: expandForMenu(menuTree, openKeys, shortcut.id),
            mobileMenuOpen: false,
        });
    }

    renderShortcutMenu(inDrawer = false) {
        const {shortcuts} = this.state;
        if (!shortcuts || shortcuts.length === 0) {
            return null;
        }

        const items = shortcuts.map(item => ({
            key: item.id,
            icon: <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 16}}/>,
            label: item.name,
        }));

        return <div className='shortcut-menu'>
            <div className='shortcut-menu-title'>快捷菜单</div>
            <Menu items={items}
                  theme={inDrawer ? 'light' : 'dark'}
                  mode="inline"
                  className='shortcut-menu-list'
                  selectable={false}
                  inlineIndent={16}
                  onClick={({key}) => {
                      const shortcut = shortcuts.find(item => item.id === key);
                      if (shortcut?.path) {
                          this.activateMenuForShortcut(shortcut);
                          this.closeMobileMenu();
                          PageUtils.open(shortcut.path);
                      }
                  }}
            />
        </div>;
    }

    renderLeftMenu(inDrawer = false) {
        if(this.state.menuLoading){
            return <div style={{padding: 16}}><Skeleton active title={false} paragraph={{rows: 8}} /></div>;
        }

        const {menuMode, isMobile, menuTree, sideMenus} = this.state;
        // 顶部菜单模式桌面端只渲染当前一级菜单的子菜单；侧边模式与移动端抽屉渲染完整树
        const items = (inDrawer || isMobile || menuMode === 'side') ? menuTree : sideMenus;

        return <Menu items={items}
                     theme={inDrawer ? 'light' : 'dark'}
                     mode="inline"
                     className='left-menu'
                     onClick={this.onLeftMenuClick}
                     selectedKeys={this.state.currentMenuKey ? [this.state.currentMenuKey] : []}
                     openKeys={this.state.openKeys}
                     onOpenChange={keys => this.setState({openKeys: keys})}
                     inlineIndent={16}
        >
        </Menu>;
    }

    /** 手机端抽屉：复刻侧栏头部 + 快捷菜单 + 完整左侧菜单树 */
    renderMobileDrawer = () => {
        const {siteInfo} = this.state;
        const siderTitle = (siteInfo.shortTitle || '').trim() || siteInfo.title || '首页';
        return (
            <Drawer className='mobile-menu-drawer'
                    placement='left'
                    size={260}
                    closable={false}
                    open={this.state.mobileMenuOpen}
                    onClose={this.closeMobileMenu}
                    styles={{body: {padding: 0}}}>
                <div className='mobile-drawer-header'>
                    <button type='button' className='logo-img'
                            onClick={() => { this.closeMobileMenu(); history.push('/'); }}>
                        <img src="./logo.png" alt={siteInfo.title || '首页'}/>
                    </button>
                    <h3>
                        <Link to="/" onClick={this.closeMobileMenu} title={siteInfo.title || siderTitle}>
                            {siderTitle}
                        </Link>
                    </h3>
                    <button type='button' className='mobile-drawer-close' aria-label='关闭菜单'
                            onClick={this.closeMobileMenu}>
                        <CloseOutlined/>
                    </button>
                </div>
                {this.renderShortcutMenu(true)}
                {this.renderLeftMenu(true)}
            </Drawer>
        );
    }

    renderCenterContent = () => {
        const {siteInfo, loginInfo, layoutMode} = this.state
        // 即使菜单为空也直接显示页面内容，不要一直显示 Skeleton
        const contentNode = layoutMode === 'single'
            ? <SinglePageLayout pathMenuMap={this.state.pathMenuMap}/>
            : <TabLayout pathMenuMap={this.state.pathMenuMap}/>;
        if (siteInfo.waterMark === true) {
            return <Watermark content={[loginInfo.name, loginInfo.account]}>
                {contentNode}
            </Watermark>
        }

        return contentNode
    };
}

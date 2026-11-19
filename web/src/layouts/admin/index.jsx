import React from 'react';
import {ConfigProvider, Drawer, Layout, Menu, Skeleton, Watermark} from 'antd';
import {CloseOutlined, MenuOutlined} from '@ant-design/icons';

import "./index.less"
import {HttpClient, NamedIcon, PageUtils, GlobalData, TreeUtils, history, Link, ARTICLE_HEADER_LEFT, OrgSwitcher, getSiderThemeConfig, getLayoutMode, EventBus} from "../../framework";

import { HeaderRight } from "./HeaderRight";
import { TabLayout } from "./TabLayout";
import { SinglePageLayout } from "./SinglePageLayout";
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

        // 统一左侧菜单：直接渲染完整菜单树，按当前页面自动展开所属目录
        openKeys: [],

        siteInfo: {},

        headerLeftArticles: [],

        layoutMode: getLayoutMode(),

        // 手机端：左侧栏完全收起，改由顶栏汉堡按钮 + 抽屉展示菜单
        isMobile: typeof window !== 'undefined' && typeof window.matchMedia === 'function'
            ? window.matchMedia('(max-width: 768px)').matches
            : false,
        mobileMenuOpen: false,
    }


    unsubscribeLayout = null;
    unsubscribeSiteInfo = null;
    unsubscribeShortcuts = null;
    mobileMediaQuery = null;
    handleMobileMediaChange = null;

    componentDidMount() {
        // 窄屏监听：进入/退出手机布局时切换左侧栏与汉堡抽屉
        if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
            this.mobileMediaQuery = window.matchMedia('(max-width: 768px)');
            this.handleMobileMediaChange = e => {
                this.setState({isMobile: e.matches});
                // 退回桌面宽度时关闭抽屉，避免残留遮罩
                if (!e.matches) {
                    this.setState({mobileMenuOpen: false});
                }
            };
            this.mobileMediaQuery.addEventListener('change', this.handleMobileMediaChange);
        }

        const siteInfo = GlobalData.getSiteInfo();
        const loginInfo = GlobalData.getLoginInfo()
        this.setState({siteInfo, loginInfo})

        this.initMenu()
        this.loadShortcuts()

        this.unsubscribeShortcuts = EventBus.on('shortcutsChange', this.loadShortcuts)

        const siteArticles = GlobalData.getSiteArticles();
        this.setState({headerLeftArticles: siteArticles[ARTICLE_HEADER_LEFT] || []})

        this.unsubscribeLayout = EventBus.on('layoutChange', () => {
            this.setState({layoutMode: getLayoutMode()});
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
        if (this.unsubscribeSiteInfo) {
            this.unsubscribeSiteInfo();
        }
        if (this.unsubscribeShortcuts) {
            this.unsubscribeShortcuts();
        }
        if (this.mobileMediaQuery && this.handleMobileMediaChange) {
            this.mobileMediaQuery.removeEventListener('change', this.handleMobileMediaChange);
        }
    }


    openMobileMenu = () => this.setState({mobileMenuOpen: true});

    closeMobileMenu = () => this.setState({mobileMenuOpen: false});

    loadShortcuts = () => {
        HttpClient.get('admin/user-center/shortcuts', null, {toastError: false})
            .then(rs => this.setState({shortcuts: rs.data || []}))
            .catch(() => this.setState({shortcuts: []}))
    }


    initMenu = () => {
        this.setState({menuLoading: true})
        HttpClient.get('/admin/menu-info', null, {toastError: false}).then(info => {
            const {menuTree, pathMenuMap, menuMap} = info.data
            this.setState({menuMap})

            const pathname = PageUtils.currentPathname();

            TreeUtils.walk(menuTree, (item) => {
                if (item.type === 'divider') {
                    return
                }
                item.icon = <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 16}}/>
            })

            // 统一左侧菜单：不再按顶部菜单切分，直接用完整菜单树
            let currentMenuKey = null;
            if (pathname !== "" && pathname !== "/") {
                const menuDef = pathMenuMap[pathname];
                if (menuDef) {
                    currentMenuKey = menuDef.id;
                }
            }
            // 默认展开当前页面所属的各级目录，否则 inline 模式下选中项不可见
            const openKeys = currentMenuKey ? (this.findAncestorKeys(menuTree, currentMenuKey) || []) : [];

            this.setState({menuTree, pathMenuMap, currentMenuKey, openKeys})
            this.setState({menuLoading: false})

        }).catch(err => {
            console.error('加载菜单失败:', err)
            this.setState({menuTree: [], openKeys: []})
            this.setState({menuLoading: false})
        })
    }
    actionRef = React.createRef()

    /** 查找某个菜单项的所有祖先 key，用于默认展开 inline 菜单对应目录 */
    findAncestorKeys = (nodes, key, chain = []) => {
        for (const node of nodes || []) {
            if (node.key === key) {
                return chain;
            }
            const found = this.findAncestorKeys(node.children, key, [...chain, node.key]);
            if (found) {
                return found;
            }
        }
        return null;
    }

    /** 展开某个菜单项所属的各级目录，同时保留用户已展开的其它目录 */
    expandForMenu = (key) => {
        const ancestors = this.findAncestorKeys(this.state.menuTree, key) || [];
        return Array.from(new Set([...this.state.openKeys, ...ancestors]));
    }

    onLeftMenuClick = ({key}) => {
        const menu = this.state.menuMap[key];
        if (!menu) return;
        const {path} = menu;
        // 手机端抽屉内点击菜单后自动收起抽屉，并展开新页面所属目录
        this.setState({currentMenuKey: key, mobileMenuOpen: false, openKeys: this.expandForMenu(key)});
        PageUtils.open(path);
    }

    // 顶部菜单已移除，保留此回调字段以兼容业务项目的 headerExtra / showOrgSwitcher 签名
    getActiveTopMenu() {
        return null;
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


    /** 点击快捷菜单时同步左侧菜单的选中项并展开所属目录 */
    activateMenuForShortcut(shortcut) {
        this.setState({
            currentMenuKey: shortcut.id,
            openKeys: this.expandForMenu(shortcut.id),
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
        return <Menu items={this.state.menuTree}
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
                    <button type="button" className='logo-img'
                            onClick={() => { this.closeMobileMenu(); history.push('/'); }}>
                        <img src="./logo.png" alt={siteInfo.title || '首页'}/>
                    </button>
                    <h3>
                        <Link to="/" onClick={this.closeMobileMenu} title={siteInfo.title || siderTitle}>
                            {siderTitle}
                        </Link>
                    </h3>
                    <button type="button" className='mobile-drawer-close' aria-label='关闭菜单'
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



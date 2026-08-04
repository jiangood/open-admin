import React from 'react';
import {Layout, Menu, Skeleton, Watermark} from 'antd';

import "./index.less"
import {HttpUtils, NamedIcon, PageUtils, GlobalData, TreeUtils, history, Link, ARTICLE_HEADER_LEFT, OrgSwitcher} from "../../framework";

import { HeaderRight } from "./HeaderRight";
import { TabLayout } from "./TabLayout";
const {Header, Sider, Content} = Layout;
/**
 * 带菜单的布局，主要处理布局宇框架结构
 */
export default class extends React.Component {

    state = {
        loginInfo: {},

        menuTree: [],
        menuMap: {},
        pathMenuMap: {},
        menuLoading:true,


        currentMenuKey: null,

        topMenus: [],
        sideMenus: [],
        activeTopMenuKey: null,

        siteInfo: {},

        headerLeftArticles: [],
    }


    componentDidMount() {
        // 判断是否手机端，自动收起菜单


        const siteInfo = GlobalData.getSiteInfo();
        const loginInfo = GlobalData.getLoginInfo()
        this.setState({siteInfo, loginInfo})

        this.initMenu()

        const siteArticles = GlobalData.getSiteArticles();
        this.setState({headerLeftArticles: siteArticles[ARTICLE_HEADER_LEFT] || []})
    }


    initMenu = () => {
        this.setState({menuLoading: true})
        HttpUtils.get('/admin/menu-info').then(info => {
            const {menuTree, pathMenuMap, menuMap} = info
            this.setState({menuMap})

            const pathname = PageUtils.currentPathname();

            TreeUtils.walk(menuTree, (item) => {
                item.icon = <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 12}}/>
            })

            const {topMenus} = this.classifyMenus(menuTree);

            // 确定当前活跃的顶部菜单和侧边菜单
            let activeTopMenuKey = null;
            let sideMenus = [];
            let currentMenuKey = null;

            if (pathname !== "" && pathname !== "/") {
                const menuDef = pathMenuMap[pathname];
                if (menuDef) {
                    currentMenuKey = menuDef.id;
                    const matched = this.findActiveTopMenu(topMenus, menuDef, menuMap);
                    if (matched) {
                        activeTopMenuKey = matched.key;
                        sideMenus = matched.children || [];
                    }
                }
            }

            // 默认选中第一个顶部菜单
            if (!activeTopMenuKey && topMenus.length > 0) {
                activeTopMenuKey = topMenus[0].key;
                sideMenus = topMenus[0].children || [];
            }

            this.setState({menuTree, pathMenuMap, topMenus, sideMenus, activeTopMenuKey, currentMenuKey})

        }).catch(err => {
            console.error('加载菜单失败:', err)
            this.setState({menuTree: [], topMenus: [], sideMenus: []})
        }).finally(()=>{
            this.setState({menuLoading: false})
        })
    }
    actionRef = React.createRef()

    classifyMenus = (menuTree) => {
        const topMenus = [];
        const leafRootNodes = [];

        (menuTree || []).forEach(node => {
            const isDir = node.type === 'directory' || (node.children && node.children.length > 0);
            if (isDir) {
                topMenus.push(node);
            } else {
                leafRootNodes.push(node);
            }
        });

        if (leafRootNodes.length > 0) {
            const defaultGroup = {
                key: '_default_group',
                label: '业务模块',
                icon: <NamedIcon name="AppstoreOutlined" style={{fontSize: 12}}/>,
                children: leafRootNodes,
                type: 'directory',
            };
            topMenus.unshift(defaultGroup);
        }

        return {topMenus};
    }

    findActiveTopMenu = (topMenus, menuDef, menuMap) => {
        if (!menuDef) return null;

        // 从 menuDef 的 pid 链向上查找匹配的 topMenu
        let pid = menuDef.pid || menuDef.parentKey;
        while (pid) {
            const match = topMenus.find(t => t.key === pid);
            if (match) return match;
            const parent = menuMap[pid];
            pid = parent ? (parent.pid || parent.parentKey) : null;
        }
        return null;
    }

    onTopMenuClick = ({key}) => {
        const topMenu = this.state.topMenus.find(t => t.key === key);
        if (!topMenu) return;

        const sideMenus = topMenu.children || [];
        this.setState({activeTopMenuKey: key, sideMenus});
    }

    onLeftMenuClick = ({key}) => {
        const menu = this.state.menuMap[key];
        if (!menu) return;
        const {path} = menu;
        this.setState({currentMenuKey: key});
        PageUtils.open(path, menu.name);
    }

    getActiveTopMenu() {
        const {topMenus, activeTopMenuKey} = this.state;
        if (!activeTopMenuKey || !topMenus) return null;
        return topMenus.find(t => t.key === activeTopMenuKey) || null;
    }

    renderTopMenu = () => {
        if (this.state.menuLoading) return null;
        const {topMenus, activeTopMenuKey} = this.state;
        if (!topMenus || topMenus.length === 0) return null;
        return (
            <nav className="top-nav">
                {topMenus.map(item => (
                    <span
                        key={item.key}
                        className={'top-nav-item' + (item.key === activeTopMenuKey ? ' active' : '')}
                        onClick={() => this.onTopMenuClick({key: item.key})}
                    >
                        {item.label}
                    </span>
                ))}
            </nav>
        );
    }

    render() {
        const {siteInfo, loginInfo} = this.state

return <Layout className='main-layout'>
             <Sider id='left-sider'
                   width={180}
                   collapsible
                   breakpoint={'md'}
             >
                <div className='sider-header'>
                    <img className='logo-img' src="./logo.png" onClick={() => history.push('/')} alt='logo'/>
                    <h3 className='hide-on-mobile'>
                        <Link to="/" style={{color: 'rgba(255,255,255,0.85)'}}>{siteInfo.title}</Link>
                    </h3>
                </div>
                {this.renderLeftMenu()}

            </Sider>

            <Layout style={{flex: 1, overflow: 'hidden'}}>
                <Header className='header'>
                    {this.renderTopMenu()}
                    {this.state.headerLeftArticles.map(a => (
                        <div key={a.code} className='item' style={{cursor: 'pointer', padding: '0 12px', whiteSpace: 'nowrap'}}
                             onClick={() => PageUtils.open('/article/' + a.code, a.title)}>{a.title}</div>
                    ))}
                    {this.props.showOrgSwitcher?.({
                        activeTopMenu: this.getActiveTopMenu(),
                        loginInfo: this.props.loginInfo,
                    }) && <OrgSwitcher/>}
                    {this.props.headerExtra?.({
                        activeTopMenu: this.getActiveTopMenu(),
                        loginInfo: this.props.loginInfo,
                    })}
                    <HeaderRight/>
                </Header>

                <Content id='admin-layout-content'>
                    {this.renderCenterContent(loginInfo)}
                </Content>

            </Layout>
        </Layout>
    }


    renderLeftMenu() {
        if(this.state.menuLoading){
            return <div style={{padding: 16}}><Skeleton active title={false} paragraph={{rows: 8}} /></div>;
        }
        return <Menu items={this.state.sideMenus}
                     theme='dark'
                     mode="inline"
                     className='left-menu'
                     onClick={this.onLeftMenuClick}
                     selectedKeys={this.state.currentMenuKey ? [this.state.currentMenuKey] : []}
                     inlineIndent={16}
        >
        </Menu>;
    }

    renderCenterContent = () => {
        const {siteInfo, loginInfo} = this.state
        // 即使菜单为空也直接显示页面内容，不要一直显示 Skeleton
        const tabPageRenderNode = <TabLayout pathMenuMap={this.state.pathMenuMap}/>;
        if (siteInfo.waterMark === true) {
            return <Watermark content={[loginInfo.name, loginInfo.account]}>
                {tabPageRenderNode}
            </Watermark>
        }

        return tabPageRenderNode
    };
}



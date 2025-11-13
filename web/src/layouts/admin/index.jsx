// 全局路由

import React from 'react';
import {Badge, Layout, Menu, Watermark} from 'antd';

import {history, Link} from 'umi';
import "./index.less"
import {MenuFoldOutlined, MenuUnfoldOutlined} from '@ant-design/icons';
import defaultLogo from '../../asserts/logo.png'
import {HttpUtil, isMobileDevice, NamedIcon, PageUtil, SysUtil, theme, TreeUtil} from "../../framework";

import HeaderRight from "./HeaderRight";
import TabPageRender from "./TabPageRender";

const {Header, Footer, Sider, Content} = Layout;
/**
 * 带菜单的布局，主要处理布局宇框架结构
 */
export default class extends React.Component {

    state = {
        loginInfo: {},

        menuTree: [],
        pathMenuMap: {},


        currentMenuKey: null,


        collapsed: false,
        siteInfo: {},
        isMobileDevice: false,
    }

    tabPageRenderRef = React.createRef()


    componentDidMount() {
        console.log('Admin Layout didMount')
        // 判断是否手机端，自动收起菜单
        if (isMobileDevice()) {
            this.setState({collapsed: true, isMobileDevice: true})
        }

        let siteInfo = SysUtil.getSiteInfo();
        const loginInfo = SysUtil.getLoginInfo()
        this.setState({siteInfo,loginInfo})

        this.initMenu()
    }


    initMenu = () => {
        HttpUtil.get('admin/menuInfo').then(info => {
            const {menuTree, pathMenuMap, badgeList} = info

            let pathname = PageUtil.currentPathname();

            TreeUtil.walk(menuTree, (item) => {
                item.icon = <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 12}}/>
            })

            if (pathname !== "" && pathname !== "/") {
                let menu = pathMenuMap[pathname]
                if(menu){
                    this.setState({currentMenuKey: menu.key})
                }
            }

            this.setState({menuTree,pathMenuMap})

            this.loadBadge(badgeList)
        })


    }
    actionRef = React.createRef()

    toggleCollapsed = (v) => {
        this.setState({collapsed: v})
    }
    loadBadge = list => {
        for (let item of list) {
            const {menuId, url} = item
            HttpUtil.get(url, null).then(rs => {
                const {leftMenus} = this.state
                const menu = TreeUtil.findByKey(menuId, leftMenus)
                if (menu) {
                    menu.icon = <Badge dot count={rs} size={"small"}>{menu.icon}</Badge>
                    this.setState({leftMenus: [...leftMenus]})
                }

            })
        }
    };




    render() {
        const {siteInfo, loginInfo} = this.state
        let logo = this.props.logo || defaultLogo
        if (siteInfo.logo) {
            logo = SysUtil.wrapServerUrl('sysFile/preview/' + siteInfo.logo)
        }


        return <Layout className='main-layout'>
            <Header className='header'>
                <div className='header-left'>

                    <img className='logo-img' src={logo} onClick={() => history.push('/')}/>
                    <h3 className='hide-on-mobile'>
                        <Link to="/" style={{color: theme["primary-color"]}}>{siteInfo.title} </Link>
                    </h3>

                </div>
                <HeaderRight/>
            </Header>

            <Layout style={{height: '100%'}}>
                <Sider id='left-sider'
                       collapsible
                       collapsed={this.state.collapsed}
                       onCollapse={(value) => this.toggleCollapsed(value)}
                       trigger={null}>

                    <div style={{
                        color: 'white', fontSize: 16, cursor: "pointer", margin: 12
                    }} title='收起/展开' onClick={() => this.toggleCollapsed(!this.state.collapsed)}>
                        {this.state.collapsed ? <MenuUnfoldOutlined/> : <MenuFoldOutlined/>}
                    </div>

                    <Menu items={this.state.menuTree}
                          theme='dark'
                          mode="inline"
                          className='left-menu'
                          onClick={({key, item}) => {
                              let {path, id} = item.props;
                              this.setState({currentMenuKey: id})
                              history.push(path)
                          }}
                          selectedKeys={[this.state.currentMenuKey]}
                          inlineIndent={16}
                    >
                    </Menu>

                </Sider>

                <Content id='admin-layout-content'>
                    {this.getContent(loginInfo)}
                </Content>

            </Layout>
        </Layout>
    }


    getContent = () => {
        const {siteInfo, loginInfo} = this.state
        if (this.state.menuTree.length === 0) { // 加载菜单中
            return <div>数据加载中...</div>
        }
        let tabPageRenderNode = <TabPageRender ref={this.tabPageRenderRef} pathMenuMap={this.state.pathMenuMap}/>;
        if (siteInfo.waterMark === true) {
            return <Watermark content={[loginInfo.name, loginInfo.account]}>
                {tabPageRenderNode}
            </Watermark>
        }

        return tabPageRenderNode
    };
}



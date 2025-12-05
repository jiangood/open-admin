// 全局路由

import React from 'react';
import {Badge, Layout, Menu, Watermark} from 'antd';

import {history, Link} from 'umi';
import "./index.less"
import {Gap, HttpUtils, NamedIcon, PageUtils, SysUtils, ThemeUtils, TreeUtils} from "../../framework";

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
        menuMap: {},
        pathMenuMap: {},


        currentMenuKey: null,


        siteInfo: {},
    }


    componentDidMount() {
        console.log('Admin Layout didMount')
        // 判断是否手机端，自动收起菜单


        let siteInfo = SysUtils.getSiteInfo();
        const loginInfo = SysUtils.getLoginInfo()
        this.setState({siteInfo, loginInfo})

        this.initMenu()
    }


    initMenu = () => {
        HttpUtils.get('/admin/menuInfo').then(info => {
            const {menuTree, pathMenuMap, menuMap} = info
            this.setState({menuMap})

            let pathname = PageUtils.currentPathname();

            TreeUtils.walk(menuTree, (item) => {
                item.icon = <NamedIcon name={item.icon || 'AppstoreOutlined'} style={{fontSize: 12}}/>
            })

            if (pathname !== "" && pathname !== "/") {
                let menu = pathMenuMap[pathname]
                if (menu) {
                    this.setState({currentMenuKey: menu.key})
                }
            }

            this.setState({menuTree, pathMenuMap})

            this.loadBadge(menuMap)
        })


    }
    actionRef = React.createRef()


    loadBadge = menuMap => {
        for (let id in menuMap) {
            const item = menuMap[id]
            const {messageCountUrl} = item;
            if (!messageCountUrl) {
                continue
            }
            HttpUtils.get(messageCountUrl).then(rs => {
                const {menuTree} = this.state
                const menu = TreeUtils.findByKey(id, menuTree, 'key')
                if (menu) {
                    menu.icon = <Badge dot count={rs} size={"small"}>{menu.icon}</Badge>
                    this.setState({menuTree: [...menuTree]})
                }

            })
        }
    };


    render() {
        const {siteInfo, loginInfo} = this.state

        return <Layout className='main-layout'>
            <Header className='header'>
                <div className='header-left'>
                    {siteInfo.logoUrl &&
                        <img className='logo-img' src='/admin/public/logo.jpg' onClick={() => history.push('/')} alt='logo'/>}
                    <h3 className='hide-on-mobile'>
                        <Link to="/" style={{color: ThemeUtils.getColor("primary-color")}}>{siteInfo.title} </Link>
                    </h3>

                </div>
                <HeaderRight/>
            </Header>

            <Layout style={{height: '100%'}}>
                <Sider id='left-sider'
                       collapsible
                       breakpoint={'md'}
                >
                    <Gap/>


                    <Menu items={this.state.menuTree}
                          theme='dark'
                          mode="inline"
                          className='left-menu'
                          onClick={({key}) => {
                              const menu = this.state.menuMap[key]
                              let {path} = menu;
                              this.setState({currentMenuKey: key})
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
            return <></>
        }
        let tabPageRenderNode = <TabPageRender pathMenuMap={this.state.pathMenuMap}/>;
        if (siteInfo.waterMark === true) {
            return <Watermark content={[loginInfo.name, loginInfo.account]}>
                {tabPageRenderNode}
            </Watermark>
        }

        return tabPageRenderNode
    };
}



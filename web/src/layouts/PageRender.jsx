import {matchRoutes, useAppData} from "umi";
import React from "react";
import {Result} from "antd";
import {UrlUtils} from "../framework";

/**
 * 通过指定 pathname 渲染页面
 * @param props
 * 为了规范，接收参数何router保持一致，
 * pathname： 路径 如 /flowable/task/form
 * search：搜索参数 如 /?id=1
 *
 * passLocation: 是否把location信息透传到真正页面
 *
 * @returns {React.JSX.Element|*}
 * @constructor
 */

let APP_DATA_CACHE = null


/**
 * 写函数组件主要为了使用hooks
 * @param props
 * @returns {Element}
 * @constructor
 */
export  function PageRender(props) {
    let {pathname, search, passLocation} = props

    let appData = useAppData()
    if(Object.keys(appData).length === 0){
        appData = APP_DATA_CACHE
    }else {
        APP_DATA_CACHE = appData;
    }

    return <_PageRender appData={appData}  pathname={pathname} search={search}
                        passLocation={passLocation}/>
}

class _PageRender extends React.Component {

    render() {
        if (this.props.passLocation) {
            return this.passLocationRender()
        }
        return this.defaultRender()
    }

    passLocationRender = () => {
        let {pathname, search,  appData} = this.props

        const map = appData.routeComponents
        const key = pathname.substring(1); // 移除第一个斜杠
        let componentType = map[key] || map[key + '/index']
        if (componentType) {
            let params =search ? UrlUtils.getParams(search): {}
            const location = {pathname, search, params}
            return React.createElement(componentType, {location});
        }
    };


    defaultRender = () => {
        let {pathname, appData} = this.props
        let matchArr = matchRoutes(appData.clientRoutes, pathname)

        if (matchArr != null) {
            if (pathname === '/') {
                // 匹配结果为1，表示未定义index.jsx ，导致死循环
                if (matchArr.length === 1) { // 如果项目中没有定义index.jsx
                    return <Result icon={null} title='未定义首页'></Result>
                }
            }
            // 取最匹配的那个
            const mathResult = matchArr[matchArr.length - 1].route
            if(mathResult){
                return mathResult.element;
            }
        }

        // 如果实在找不到页面组件，则404
        return <Result
            status={404}
            title='页面不存在！'
            subTitle={<div>路由地址：{pathname}</div>}
        />
    };
}

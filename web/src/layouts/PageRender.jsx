import {matchRoutes, useAppData} from "umi";
import React from "react";
import {Result} from "antd";

/**
 * 通过指定 pathname 渲染页面
 * @param {object} props
 * @param {string} props.pathname - 路径 如 /flowable/task/form
 */
export function PageRender({pathname}) {
    const appData = useAppData()
    const matchArr = matchRoutes(appData.clientRoutes, pathname)

    if (matchArr != null) {
        const matchResult = matchArr[matchArr.length - 1].route
        if (matchResult && matchResult.element) {
            return matchResult.element
        }
    }

    return (
        <Result
            status={404}
            title='页面不存在！'
            subTitle={<div>路由地址：{pathname}</div>}
        />
    )
}
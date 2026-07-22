import {matchRoutes, useAppData} from "umi";
import {Result} from "antd";

interface PageRenderProps {
    pathname: string;
}

/**
 * 通过指定 pathname 渲染页面
 */
export function PageRender({pathname}: PageRenderProps) {
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

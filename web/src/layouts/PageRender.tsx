import {matchRoute} from "../framework";
import {Result} from "antd";

interface PageRenderProps {
    pathname: string;
}

/**
 * 通过指定 pathname 渲染页面
 */
export function PageRender({pathname}: PageRenderProps) {
    const matched = matchRoute(pathname);

    if (matched) {
        const {component: Comp, params} = matched;
        return <Comp params={params}/>;
    }

    return (
        <Result
            status={404}
            title='页面不存在！'
            subTitle={<div>路由地址：{pathname}</div>}
        />
    )
}

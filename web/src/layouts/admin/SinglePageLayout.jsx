import React from "react";
import {history, PageFrame, matchRoute, setPageTitle} from "../../framework";

/**
 * 单页布局：不使用页签，仅渲染当前路由对应页面。
 * 路由变化时按 url 重新挂载 PageFrame，页面状态随之重置。
 */
export class SinglePageLayout extends React.Component {
    state = {location: history.location};

    unlisten = null;

    componentDidMount() {
        this.unlisten = history.listen(({location}) => {
            this.setState({location});
        });
        this.syncDocumentTitle();
    }

    componentDidUpdate() {
        this.syncDocumentTitle();
    }

    componentWillUnmount() {
        if (this.unlisten) {
            this.unlisten();
        }
    }

    syncDocumentTitle() {
        const {pathname} = this.state.location;
        const label = matchRoute(pathname)?.meta?.title || this.props.pathMenuMap?.[pathname]?.name;
        setPageTitle(label);
    }

    render() {
        const {pathname, search} = this.state.location;
        const url = pathname + search;
        return (
            <div style={{height: '100%'}}>
                <PageFrame key={url} url={url} show/>
            </div>
        );
    }
}

export default SinglePageLayout;

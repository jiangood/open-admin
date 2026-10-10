import React from "react";
import {history, PageFrame} from "../../framework";

/**
 * 单页布局：不使用标签页，仅渲染当前路由对应页面。
 * 路由变化时按 url 重新挂载 PageFrame，页面状态随之重置。
 */
export class SinglePageLayout extends React.Component {
    state = {location: history.location};

    unlisten = null;

    componentDidMount() {
        this.unlisten = history.listen(({location}) => {
            this.setState({location});
        });
    }

    componentWillUnmount() {
        if (this.unlisten) {
            this.unlisten();
        }
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

import React from "react";
import {HttpUtils, Page} from "../framework";

export default class extends React.Component {

    state = {
        buildInfo: null,
    }

    componentDidMount() {
        HttpUtils.get('/admin/lib-info').then(data => {
            this.setState({buildInfo: data})
        }).catch(e => {
            console.error('[About] 加载版本信息失败:', e);
        })
    }

    render() {
        const {buildInfo} = this.state;

        return (
            <Page title="关于" description="了解系统版本与相关信息">
                <h1>关于</h1>
                {buildInfo && (
                    <div style={{marginTop: '3em', color: 'rgba(0,0,0,0.45)', fontSize: 12}}>
                        依赖库版本 v{buildInfo.version} / {buildInfo.buildTime}
                    </div>
                )}
            </Page>
        );
    }
}

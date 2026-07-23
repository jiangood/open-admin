import React from "react";
import {Card, Descriptions} from "antd";
import {HttpUtils, Page} from "../framework";

export default class extends React.Component {

    state = {
        buildInfo: null,
    }

    componentDidMount() {
        HttpUtils.get('/admin/build-info').then(data => {
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
                    <Card title="版本信息" size="small" style={{maxWidth: 500, marginTop: 16}}>
                        <Descriptions column={1} size="small">
                            <Descriptions.Item label="框架版本">
                                {buildInfo.version}
                            </Descriptions.Item>
                            <Descriptions.Item label="构建时间">
                                {buildInfo.buildTime}
                            </Descriptions.Item>
                        </Descriptions>
                    </Card>
                )}
            </Page>
        );
    }
}

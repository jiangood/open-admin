import React from "react";
import {Card} from "antd";
import {Page} from "../framework";

export default class HomePage extends React.Component {
    render() {
        return <Page title="首页" description="欢迎使用本系统">
            <Card>欢迎使用本系统</Card>
        </Page>
    }
}

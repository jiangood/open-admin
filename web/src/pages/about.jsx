import React from "react";
import {history} from "umi";
import {Page} from "../framework";

export default class extends React.Component {

    componentDidMount() {
        history.push('/article/about')
    }

    render() {
        return <Page title="关于系统">
            <div>跳转中...</div>
        </Page>
    }
}

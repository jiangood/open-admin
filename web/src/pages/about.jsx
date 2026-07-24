import React from "react";
import {Page, history} from "../framework";

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

import React from "react";
import {Result} from "antd";
import {Page} from "../framework";

export default class NotFound extends React.Component {

    render() {
        return <Page>
            <Result
                status={404}
                title='页面不存在'
            />
        </Page>
    }
}

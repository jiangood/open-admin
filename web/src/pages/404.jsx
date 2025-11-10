import React from "react";
import {Result} from "antd";

export default class extends React.Component {

    render() {
            return  <Result
                status={404}
                title='页面不存在'
            />

    }
}

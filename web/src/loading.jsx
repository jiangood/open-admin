import React from "react";
import {Spin} from "antd";
import {theme} from "@tmgg/tmgg-commons-lang";

export default class extends React.Component {
    render() {
        return <div
            style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100vh',
                background: theme["background-color"]
            }}>
            <Spin size='large'/>
        </div>
    }
}

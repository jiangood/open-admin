import React from "react";
import {Alert, Spin} from "antd";
import {ThemeUtils} from "../../utils";


export function PageLoading(props) {
    const msg = props.message || '页面加载中...';

    return <div style={{
        height: '100vh', width: '100%',
        display: 'flex', alignItems: 'center', justifyContent: "center",
        color: ThemeUtils.getColor("primary-color")
    }}>
        <div style={{textAlign: "center", marginTop: '-10rem'}}>
            <div>
                <Spin size={"large"}></Spin>
            </div>
            <div style={{marginTop: '1rem'}}>
                <Alert title={msg}></Alert>
            </div>


        </div>
    </div>
}

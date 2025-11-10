import React from "react";
import {Button, Input, Modal, Row} from "antd";
import {FieldUploadFile} from "../upload";


export  class FieldImageBase64 extends React.Component {

    render() {
        let url = '/utils/fileBase64';
        return <>
            <Input.TextArea rows={3} value={this.props.value}></Input.TextArea>
            <FieldUploadFile url={url} onChange={this.onFinish}></FieldUploadFile>
        </>
    }

    onFinish = value => {
        if(this.props.onChange){
            this.props.onChange(value)
        }
    }

}

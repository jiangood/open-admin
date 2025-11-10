import React from "react";
import {Button, Modal, Spin} from "antd";
import {HttpUtil} from "../system";


export class DownloadFileButton extends React.Component {

    state = {
        open:false
    }

    onClick(){
        const {url, params, children, ...rest} = this.props;
        this.setState({open:true})
        HttpUtil.downloadFile(url,params).then(rs=>{

        }).finally(()=>{
            this.setState({open:false})
        })
    }

    render() {
        const {url, params, children, ...rest} = this.props;


        return <> <Button {...rest}>{children}</Button>

            <Modal title='提示' footer={null} onCancel={()=>{this.setState({open:false})}} maskClosable={false}>
                <Spin /> 下载文件中，请勿关闭浏览器
            </Modal>
        </>
    }
}

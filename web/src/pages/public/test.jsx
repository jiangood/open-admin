import React from "react";
import {Page} from "../../framework";
import {Button, message, Modal, Space} from "antd";

export default class TestPage extends React.Component {

    state = {
        modalOpen:false
    }

    render() {
        return <Page title="测试组件">

            <Space>
           <Button type={"primary"} onClick={()=>{message.info('你好，李白！')}} >message.info</Button>

            <Button danger onClick={()=>{Modal.info({content:'你好，李白！'})}} >modal.info</Button>

                <Button  onClick={()=>{this.setState({modalOpen:true})}} >modal.组件</Button>

            </Space>

            <Modal title='modal组件' open={this.state.modalOpen} onCancel={()=>this.setState({modalOpen:false})}>
                内容内容
            </Modal>


        </Page>
    }
}

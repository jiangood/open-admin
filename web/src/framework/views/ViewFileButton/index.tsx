import React from "react";
import {Button, Modal} from "antd";
import { ViewFile } from "../ViewFile";

/**
 * 文件按钮查看组件
 */
export class ViewFileButton extends React.Component {
    state = { open: false }

    render() {
        const {value} = this.props;
        if (!value) {
            return null;
        }
        
        return <>
            <Button type="link" size="small" onClick={() => this.setState({open: true})}>
                查看文件
            </Button>
            <Modal open={this.state.open} title="文件预览" footer={null} width="80vw"
                   onCancel={() => this.setState({open: false})}>
                <ViewFile value={this.props.value} />
            </Modal>
        </>;
    }
}

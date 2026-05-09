import React from "react";
import {Button} from "antd";
import { MessageUtils } from "../../utils";
import { ViewFile } from "../ViewFile";

/**
 * 文件按钮查看组件
 */
export class ViewFileButton extends React.Component {
    render() {
        const {value, height} = this.props;
        if (!value) {
            return null;
        }
        
        return <Button type="link" size="small" onClick={()=>{
            MessageUtils.alert(<ViewFile value={this.props.value} />)
        }}>
            查看文件
        </Button>;
    }
}

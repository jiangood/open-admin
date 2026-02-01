import React from "react";
import {Typography, Button, Modal, Popover} from "antd";
import {StringUtils} from "../../utils";

export class ViewText extends React.Component {

    static defaultProps = {
        ellipsis: false,
        maxLength: 15
    }

    render() {
        const {value, ellipsis, maxLength} = this.props;
        
        if(value == null){
            return null;
        }
        
        // 如果没有启用 ellipsis，使用普通文本显示
        if (!ellipsis) {
            return <Typography.Text> {value}</Typography.Text>;
        }
        
        // 如果启用了 ellipsis，使用省略功能
        const short = StringUtils.ellipsis(value, maxLength);
        return (
            <Popover placement="topLeft" title={'长文本'} content={<Button onClick={this.showModal}>点击查看全部内容</Button>}>
                {short}
            </Popover>
        );
    }

    showModal = () => {
        const {value} = this.props;
        Modal.info({
            icon: null,
            title: '长文本内容',
            content: <div style={{height: 500, overflowY: 'auto'}}>{value}</div>,
            width: 700
        });
    };
}
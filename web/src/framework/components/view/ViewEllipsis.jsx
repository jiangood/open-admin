import React from 'react';
import {Button, Modal, Popover, Tooltip} from 'antd';
import {StrUtil} from "@tmgg/tmgg-commons-lang";


export class ViewEllipsis extends React.Component {

    static defaultProps = {maxLength: 15}

    render() {
        let {maxLength, value} = this.props;

        const short = StrUtil.ellipsis(value, maxLength)
        return  <Popover placement="topLeft" title={'长文本'} content={<Button onClick={this.showModal}>点击查看全部内容</Button>}>
            {short}
        </Popover>
    }


    showModal = () => {
        let {maxLength, value} = this.props;
        Modal.info({
            icon:null,
            title:'长文本内容',
            content: <div style={{height:500,overflowY:'auto'}}>{value}</div>,
            width:700
        })
    };

}

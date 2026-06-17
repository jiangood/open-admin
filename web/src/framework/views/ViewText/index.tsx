import React from "react";
import {Typography, Tooltip, Modal} from "antd";
import {StringUtils} from "../../utils";

export class ViewText extends React.Component {

    static defaultProps = {
        ellipsis: false,
        maxLength: 15
    }

    render() {
        const {value, ellipsis, maxLength} = this.props;

        if (value == null) {
            return null;
        }

        if (!ellipsis) {
            return <Typography.Text>{value}</Typography.Text>;
        }

        const short = StringUtils.ellipsis(value, maxLength);
        return (
            <Tooltip placement="topLeft" title={value}>
                <span style={{cursor: 'pointer', borderBottom: '1px dashed #d9d9d9'}} onClick={this.showModal}>
                    {short}
                </span>
            </Tooltip>
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
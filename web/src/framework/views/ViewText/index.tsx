import React from "react";
import {Typography, Tooltip, Modal} from "antd";
import {StringUtils} from "../../utils";

export class ViewText extends React.Component {

    state = {
        modalOpen: false,
    };

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
                <button type="button"
                        style={{
                            background: 'none',
                            border: 'none',
                            borderBottom: '1px dashed #d9d9d9',
                            padding: 0,
                            font: 'inherit',
                            cursor: 'pointer',
                        }}
                        onClick={() => this.setState({modalOpen: true})}>
                    {short}
                </button>
                <Modal open={this.state.modalOpen} title="长文本内容" width={700} footer={null}
                       onCancel={() => this.setState({modalOpen: false})}>
                    <div style={{height: 500, overflowY: 'auto'}}>{value}</div>
                </Modal>
            </Tooltip>
        );
    }
}
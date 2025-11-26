import React from "react";
import {Input, message, Modal, Typography} from "antd";

export class MsgBox {

    static _ref = React.createRef();


    static toast(msg, success=true){
        if(success){
            message.success(msg)
        }else {
            message.error(msg)
        }

    }

    static alert(msg, title = '提示',modalProps = {}) {
        let instance = this._ref.current;
        if (instance) {
            return new Promise((resolve) => {
                instance.show('alert', msg, title, resolve);
            });
        }
        return Promise.resolve(false);

    }

    static confirm(msg, title = '确认') {
        console.log('confirm', this._ref.current)
        let instance = this._ref.current;
        if (instance) {
            return new Promise((resolve) => {
                instance.show('confirm', msg, title, resolve);
            });
        }else {

        }
        return Promise.resolve(false);
    }

    static prompt(msg, defaultValue = '', title = '输入') {
        let instance = this._ref.current;
        if (instance) {
            return new Promise((resolve) => {
                instance.show('prompt', msg, title, resolve);
            });
        }
        return Promise.resolve(null);
    }
}

export class MsgBoxComponent extends React.Component {

    state = {
        open: false,
        title: '提示',
        type: 'alert',
        msg: null,
        inputValue: '',
    }

    resolvePromise = null; //

    componentDidMount() {
        MsgBox._ref.current = this;
    }

    componentWillUnmount() {
        if (MsgBox._ref.current === this) {
            MsgBox._ref.current = null;
        }
    }

    show = (type, msg, title, resolvePromise) => {
        this.setState({
            open: true,
            type: type,
            msg: msg,
            title: title,
            inputValue: type === 'prompt' ? (this.state.inputValue || '') : '',
        });
        this.resolvePromise = resolvePromise
    }

    handleOk = () => {
        const {type, inputValue} = this.state;
        const  resolvePromise = this.resolvePromise

        this.setState({open: false});

        if (resolvePromise) {
            switch (type) {
                case 'confirm':
                    resolvePromise(true);
                    break;
                case 'prompt':
                    resolvePromise(inputValue);
                    break;
                default:
                    resolvePromise(null);
                    break;
            }
        }
    }

    handleCancel = () => {
        const {type, resolvePromise} = this.state;

        this.setState({open: false});

        if (resolvePromise) {
            switch (type) {
                case 'confirm':
                    resolvePromise(false);
                    break;
                case 'prompt':
                    resolvePromise(null);
                    break;
                default:
                    resolvePromise(null);
                    break;
            }
        }
    }

    handleInputChange = (e) => {
        this.setState({inputValue: e.target.value});
    }

    renderContent = () => {
        const {type, msg, inputValue} = this.state;

        switch (type) {
            case 'alert':
            case 'confirm':
                return <pre>{msg}</pre>;
            case 'prompt':
                return (
                    <>
                        <Typography.Text>{msg}</Typography.Text>
                        <Input
                            value={inputValue}
                            onChange={this.handleInputChange}
                            placeholder="请输入内容"
                        />
                    </>
                );
            default:
                return null;
        }
    }
    render() {
        const {type} = this.state;

        return (
            <Modal
                open={this.state.open}
                title={this.state.title}
                destroyOnHidden={true}
                maskClosable={false}
                onOk={this.handleOk}
                onCancel={this.handleCancel}
                okText='确定'
                cancelText='取消'
                width={400}
                transitionName=""
                maskTransitionName=""
                footer={(node, {OkBtn, CancelBtn}) => {
                    if (type === 'alert') {
                        return <OkBtn/> // 不要取消按钮
                    }
                    return node;
                }}
            >
                {this.renderContent()}
            </Modal>
        );
    }
}

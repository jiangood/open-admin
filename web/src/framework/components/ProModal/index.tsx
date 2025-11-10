import React, {ReactNode} from 'react';
import { Modal } from 'antd';

export interface ProModalProps {
  title: string;
  actionRef?: any; // 弃用，请使用ref
  ref: any;
  onShow?: any;
  footer?: any;
  width?: number;
  children:ReactNode;
}
export class ProModal extends React.Component<ProModalProps, any> {

  constructor(props:ProModalProps) {
    super(props);
    if (props.actionRef) {
      props.actionRef.current = this;
    }
    if (props.ref) {
      props.ref.current = this;
    }
  }



  state = {
    visible: false,
  };

  show = () => {
    this.setState({
      visible: true,
    });
    if (this.props.onShow) {
      this.props.onShow();
    }
  };
  hide = () => {
    this.setState({
      visible: false,
    });
  };
  open = () => {
    this.show();
  };
  close = () => {
    this.hide();
  };

  render() {
    return (
      <Modal
        maskClosable={false}
        destroyOnHidden
        title={this.props.title}
        open={this.state.visible}
        onCancel={this.hide}
        footer={this.props.footer || null}
        width={this.props.width || 800}
      >
        {this.state.visible && this.props.children}
      </Modal>
    );
  }
}

import {Form, FormInstance, Modal} from 'antd'
import React from 'react'

export interface FormModalProps {
  title: string
  onFinish?: (values: Record<string, any>) => Promise<void> | void
  onValuesChange?: (changedValues: any, allValues: any) => void
  width?: number
  labelCol?: Record<string, any>
  children?: React.ReactNode
}

export default class FormModal extends React.Component<FormModalProps> {

  state = {
    visible: false,
    confirmLoading: false,
  }

  private pendingValues: Record<string, any> | null = null
  private formRef = React.createRef<FormInstance>()

  get formInstance() {
    return this.formRef.current
  }

  open = (values?: Record<string, any>) => {
    this.pendingValues = values || {}
    this.setState({visible: true})
  }

  componentDidUpdate(_: Readonly<FormModalProps>, prevState: Readonly<{ visible: boolean; confirmLoading: boolean }>) {
    if (this.state.visible && !prevState.visible) {
      setTimeout(() => {
        if (this.pendingValues && Object.keys(this.pendingValues).length > 0) {
          this.formRef.current?.setFieldsValue(this.pendingValues)
        } else {
          this.formRef.current?.resetFields()
        }
        this.pendingValues = null
      })
    }
  }

  private handleOk = async () => {
    this.setState({confirmLoading: true})
    try {
      const values = await this.formRef.current?.validateFields()
      if (values == null) return
      await this.props.onFinish?.(values)
      this.setState({visible: false})
    } finally {
      this.setState({confirmLoading: false})
    }
  }

  render() {
    return (
      <Modal
        title={this.props.title}
        open={this.state.visible}
        confirmLoading={this.state.confirmLoading}
        onOk={this.handleOk}
        onCancel={() => this.setState({visible: false})}
        destroyOnHidden
        width={this.props.width ?? 600}
      >
        <Form ref={this.formRef} labelCol={this.props.labelCol ?? {flex: '100px'}}
              onValuesChange={this.props.onValuesChange}>
          <Form.Item name='id' noStyle/>
          {this.props.children}
        </Form>
      </Modal>
    )
  }
}

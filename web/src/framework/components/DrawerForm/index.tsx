import {Drawer, Form, FormInstance} from 'antd'
import React from 'react'

export interface FormDrawerProps {
  title: string
  onFinish?: (values: Record<string, unknown>) => Promise<void> | void
  onValuesChange?: (changedValues: Record<string, unknown>, allValues: Record<string, unknown>) => void
  width?: number
  height?: number
  placement?: 'left' | 'right' | 'top' | 'bottom'
  layout?: 'horizontal' | 'vertical' | 'inline'
  labelCol?: Record<string, unknown>
  wrapperCol?: Record<string, unknown>
  extra?: React.ReactNode
  children?: React.ReactNode
}

export default class FormDrawer extends React.Component<FormDrawerProps> {

  readonly state = {
    visible: false,
    confirmLoading: false,
  }

  private pendingValues: Record<string, unknown> | null = null
  private formRef = React.createRef<FormInstance>()

  get formInstance() {
    return this.formRef.current
  }

  open = (values?: Record<string, unknown>) => {
    this.pendingValues = values || {}
    this.setState({visible: true})
  }

  componentDidUpdate(_: Readonly<FormDrawerProps>, prevState: Readonly<{ visible: boolean; confirmLoading: boolean }>) {
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

  private handleSubmit = async () => {
    this.setState({confirmLoading: true})
    try {
      const values = await this.formRef.current?.validateFields()
      if (values == null) return
      await this.props.onFinish?.(values)
      this.setState({visible: false})
    } catch {
      // 提交失败（错误提示已由 HttpClient 弹出），保持抽屉打开以便用户修改后重试
    } finally {
      this.setState({confirmLoading: false})
    }
  }

  private footer = () => (
    <div style={{textAlign: 'right'}}>
      <button
        type='button'
        className='ant-btn ant-btn-default'
        onClick={() => this.setState({visible: false})}
      >
        取消
      </button>
      <button
        type='button'
        className='ant-btn ant-btn-primary'
        style={{marginLeft: 8}}
        disabled={this.state.confirmLoading}
        onClick={this.handleSubmit}
      >
        {this.state.confirmLoading ? '提交中...' : '确定'}
      </button>
    </div>
  )

  render() {
    return (
      <Drawer
        title={this.props.title}
        open={this.state.visible}
        onClose={() => this.setState({visible: false})}
        destroyOnHidden
        width={this.props.placement === 'top' || this.props.placement === 'bottom' ? undefined : (this.props.width ?? 600)}
        height={this.props.placement === 'top' || this.props.placement === 'bottom' ? (this.props.height ?? 600) : undefined}
        placement={this.props.placement ?? 'right'}
        footer={this.footer()}
      >
        <Form ref={this.formRef}
              layout={this.props.layout ?? 'vertical'}
              labelCol={this.props.labelCol}
              wrapperCol={this.props.wrapperCol}
              onValuesChange={this.props.onValuesChange}>
          <Form.Item name='id' noStyle/>
          {this.props.children}
        </Form>
      </Drawer>
    )
  }
}

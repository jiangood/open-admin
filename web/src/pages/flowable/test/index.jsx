import React from "react";
import {Button, Card, Form, Input, message} from "antd";
import {HttpUtil, PageLoading, PageUtil} from "@tmgg/tmgg-base";
import {StrUtil} from "@tmgg/tmgg-commons-lang";

export default class extends React.Component {

  state = {
    model: undefined
  }


  componentDidMount() {
    let params = PageUtil.currentLocationQuery()
    const id = this.id = params.id

    HttpUtil.get('flowable/test/get', {id}).then(rs=>{
        this.setState({model: rs})

    })

  }


  render() {
    if(this.state.model === undefined){
      return <PageLoading />
    }

    return <Card title={'流程测试 / 【' + this.state.model.name + "】 / " + this.state.model.code }>
      <Form onFinish={this.onFinish} layout='vertical' >
        <Form.Item name='id' label='业务标识(相当于业务表的id)' rules={[{required: true}]} initialValue={1}>
          <Input />
        </Form.Item>

        {this.state.model.conditionVariableList.map(item=><Form.Item key={item.name} name={item.name} label={item.label}>
          <Input />
        </Form.Item>)}



        <Form.Item label='   ' colon={false}>
          <Button htmlType="submit" type='primary'>提交</Button>
        </Form.Item>
      </Form>
    </Card>
  }

  onFinish = values => {
    values.modelCode = this.state.model.code
    HttpUtil.post('flowable/test/submit', values).then(rs=>{

    })
  };
}

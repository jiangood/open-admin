import React from 'react';
import {Form, Select} from 'antd';
import { HttpUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {
  bo = null;

  state = {
    data: {},

    selectOptions: [],

    javaDelegateOptions:[]
  }

  constructor(props) {
    super(props);
    this.bo = props.bo;

    const data = {};
    data['flowable:delegateExpression'] = this.bo.get('flowable:delegateExpression');
    this.state.data = data;
  }

  componentDidMount() {
    HttpUtil.get('flowable/model/javaDelegateOptions').then(rs=>{
      this.setState({javaDelegateOptions:rs})

    })
  }

  onValuesChange = (changedValue) => {
    for (let key in changedValue) {
      this.bo.set(key, changedValue[key]);
    }
  };

  render() {
    return (
      <div>
        <Form
          onValuesChange={this.onValuesChange}
          initialValues={this.state.data}
          layout={'vertical'}
        >


          <Form.Item label="监听类(表达式)" name="flowable:delegateExpression" extra="需实现JavaDelegate接口">
            <Select options={this.state.javaDelegateOptions} ></Select>
          </Form.Item>
        </Form>
      </div>
    );
  }
}

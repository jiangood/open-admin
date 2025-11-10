import React from 'react';
import {Form, Input} from 'antd';

export default class TimerEventDefinitionForm extends React.Component {
  bo = null;

  state = {
    data: {},
  };

  constructor(props) {
    super(props);
    this.bo = props.bo;

    const data = {};

    this.state.data = data;
  }

  onValuesChange = (changedValue) => {};

  render() {
    return (
      <div>
        <a
          href="https://docs.camunda.org/manual/7.16/reference/bpmn20/events/timer-events/"
          target="_blank"
        >
          参考文档1
        </a>
        &nbsp;
        <a
          href="https://docs.camunda.io/docs/components/modeler/bpmn/timer-events/"
          target="_blank"
        >
          参考文档2
        </a>
        <Form
          onValuesChange={this.onValuesChange}
          initialValues={this.state.data}
          layout={'vertical'}
        >
          <Form.Item
            label="开始时间 timeDate"
            name="timeDate"
            extra="触发时间 如2019-10-01T12:00:00Z"
          >
            <Input />
          </Form.Item>

          <Form.Item label="持续时间 timeDuration" name="timeDuration" extra="PnYnMnDTnHnMnS">
            <Input />
          </Form.Item>

          <Form.Item label="循环时间 timeCycle" name="timeCycle" extra="包含持续时间和次数 如PT5M">
            <Input />
          </Form.Item>
        </Form>
      </div>
    );
  }
}

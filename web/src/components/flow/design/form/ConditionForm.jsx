import React from 'react';
import {Button, Form, Input, InputNumber, message, Radio, Switch, Table} from 'antd';
import {DeleteOutlined, PlusOutlined} from '@ant-design/icons';
import {ProModal} from "@tmgg/tmgg-base";
import BpmnUtils from "../../BpmnUtils";
import {ArrUtil} from "@tmgg/tmgg-commons-lang";

/**
 * 条件表单
 * onFinish
 *
 */


const PREFIX = 'flowable:';


const metaInfo = {
  text: {
    ops: {
      '==': '等于',
      '!=': '不等于',
      contains: '包含',
      '!contains': '不包含',
      startWith: '开头等于',
      endWith: '结尾等于',
    },
    render() {
      return <Input />;
    },
  },
  digit: {
    ops: {
      '==': '等于',
      '!=': '不等于',
      '>': '大于',
      '<': '小于',
      '>=': '大于等于',
      '<=': '小于等于',
      between: '介于',
    },
    render(op) {
      if (op != 'between') return <InputNumber />;

      return (
        <Input.Group>
          <InputNumber placeholder="最小值"></InputNumber>
          <InputNumber placeholder="最大值"></InputNumber>
        </Input.Group>
      );
    },
  },
  boolean: {
    ops: {
      '==': '等于',
    },
    render() {
      return <InputNumber />;
    },
  },
};

export default class extends React.Component {

constructor(props) {
  super(props);





  let conditionList = this.props.bo.get('flowable:conditionList');
  conditionList = conditionList ? JSON.parse(conditionList) : []
  this.state.conditionList = conditionList;
  this.state.expression = this.createExpression(conditionList)
}

  state = {
    expression: null,
    conditionList:[],
    modalForm: {
      key: null,
      keyLabel: null,

      valueType: null,

      op: null,
      opLabel: null,

      value: null,
      valueLabel: null,
    },
  };

  addRef = React.createRef();

  add = () => {
    this.setState({ modalForm: {} });
    this.addRef.current.show();
  };

  handleAdd = (values) => {
    const { modalForm } = this.state;
    const { conditionList } = this.state;
    modalForm.value = values.value;
    conditionList.push(modalForm);
    this.save(conditionList);
    this.addRef.current.close();
  };

  handleDelete = (item) => {
    let { conditionList } = this.state;

    ArrUtil.remove(conditionList, item)
    this.save(conditionList);
  };

  save = (conditionList) => {
    const expression = this.createExpression(conditionList);

    this.setState({ conditionList, expression }, ()=>this.setNodeLabel());
    this.setConditionData(expression, conditionList);
  };

  createExpression(conditionList) {
    const expressionList = [];
    conditionList.forEach((i) => {
      expressionList.push(this.createExpressionByCondition(i));
    });
    const expression = expressionList.join('&&');

    console.log('条件表达式', expression);

    return expression;
  }

  /**
   *  创建表达式
   * key: "days"
   * keyLabel: "请假天数"
   * op: "=="
   * opLabel: "等于"
   * value: 34
   * valueType: "digit", "text"
   *
   * @param i
   * @returns {*}
   */
  createExpressionByCondition(condition) {
    const { key, op, value, valueType } = condition;
    switch (valueType) {
      case 'digit':
        return key + op + value;
      case 'text':
        if (op === 'contains') {
          return key + ".contains('" + value + "')";
        }
        if (op === '!contains') {
          return '!' + key + ".contains('" + value + "')";
        }
        return key + op + "'" + value + "'";
      default:
        throw new Error('未知类型' + valueType);
    }
  }

  columns = [
    {
      dataIndex: 'key',
      title: '名',
      render(_, r) {
        return r.keyLabel;
      },
    },
    {
      dataIndex: 'opLabel',
      title: '操作',
    },
    {
      dataIndex: 'value',
      title: '值',
      render(_, r) {
        if (r.valueLabel != null) {
          return r.valueLabel;
        }
        return r.value;
      },
    },
    {
      dataIndex: 'option',
      title: '-',
      render: (_, record) => (
        <a onClick={() => this.handleDelete(record)}>
          <DeleteOutlined />
        </a>
      ),
    },
  ];

  setConditionData = (expression, conditionList) => {
    if (expression != null && expression != '') {
      expression = '${' + expression + '}'
    }

    // 设置标准条件表达式
    const conditionExpression = this.props.moddle.create('bpmn:FormalExpression', {body: expression});

    this.props.modeling.updateProperties(this.props.node, {conditionExpression: conditionExpression});

    // 保存条件列表 （最总存在xml中）
    this.props.bo.set('flowable:conditionList', JSON.stringify(conditionList))
    message.success("设置成功")
  }

  render() {
    const { conditionVariable } = this.props;

    if (conditionVariable == null || conditionVariable.length == 0) {
      return '未设置条件变量，需在后台XML';
    }

    const { modalForm } = this.state;
    let { conditionList } = this.state;

    let { valueType, op, opLabel, value } = modalForm;

    const meta = metaInfo[valueType];

    conditionList = JSON.parse(JSON.stringify(conditionList)); // 这样才能触发dataSource变化

    return (
      <>
        <Button type="primary" onClick={this.add} icon={<PlusOutlined />}>
           添加条件
        </Button>
        <div className="q-my-sm" />
        <Table
          dataSource={conditionList}
          columns={this.columns}
          pagination={false}
          size="small"
          bordered
          rowKey="key"
        ></Table>


        <div className="q-my-md"></div>

        <div>条件表达式: {this.state.expression}</div>
        <div className="q-my-md"></div>


        <ProModal actionRef={this.addRef} title="添加条件">
          <Form onFinish={this.handleAdd} layout="vertical">
            <Form.Item name="item" label="数据" rules={[{ required: true }]}>
              <Radio.Group
                onChange={(e) => {
                  const form = {
                    key: e.target.value.name,
                    keyLabel: e.target.value.label,
                    valueType: e.target.value.valueType,
                  };
                  this.setState({ modalForm: form });
                }}
              >
                {conditionVariable.map((v) => (
                  <Radio key={v.name} name={v.name} value={v}>
                    {v.label}
                  </Radio>
                ))}
              </Radio.Group>
            </Form.Item>

            {meta && (
              <Form.Item name="op" label="符号" rules={[{ required: true }]}>
                <Radio.Group
                  onChange={(e) => {
                    op = e.target.value;
                    opLabel = meta.ops[op];
                    if (value != null && (op == 'between' || value.indexOf(' - ') >= 0)) {
                      value = null;
                    }
                    modalForm.op = op;
                    modalForm.opLabel = opLabel;
                    modalForm.value = value;
                    this.setState({ modalForm });
                  }}
                >
                  {Object.keys(meta.ops).map((k) => {
                    const kv = {
                      key: k,
                      value: meta.ops[k],
                    };
                    return (
                      <Radio key={k} value={k}>
                        {kv.value}
                      </Radio>
                    );
                  })}
                </Radio.Group>
              </Form.Item>
            )}

            {meta && (
              <Form.Item name="value" label="值" rules={[{ required: true }]}>
                {meta.render(op)}
              </Form.Item>
            )}

            <Button type="primary" htmlType="submit">
              确定
            </Button>
          </Form>
        </ProModal>
      </>
    );
  }
}

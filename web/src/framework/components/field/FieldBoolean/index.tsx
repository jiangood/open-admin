/**
 * antd 自带的checkbox 未选择时不会返回false, 和form配合时还得设置 valuePropName
 *
 *
 * 本组件改成布尔值的输入
 *
 * 选中 true， 反选 false
 *
 * 注意：null 会转为false
 *
 *
 */
import React from 'react';
import {Checkbox, Radio} from 'antd';
import {FieldProps} from "../FieldProps";


export {FieldCheckboxBoolean as FieldBoolean};

export class FieldCheckboxBoolean extends React.Component<FieldProps, any> {


  render() {

    let {value, mode} = this.props

    const checked = this.parseBoolean(value);

    if (mode == 'read') {
      if (checked !== null) {
        return checked ? "是" : "否"
      }
    }


    return (
      <Checkbox
        checked={checked}
        onChange={(e) => {
          if (this.props.onChange) {
            this.props.onChange(!!e.target.checked);
          }
        }}
      />
    );
  }

  parseBoolean(v: any) {
    if (v === null) {
      return undefined;
    }

    if (typeof v === 'boolean') {
      return v;
    }
    return v === 1 || v === 'true' || v === 'Y';

  }
}


export class FieldRadioBoolean extends React.Component {
  render() {
    let {mode, value, onChange} = this.props;

    if(value != null){
      // 转换一下，以免一些字符串格式出现
      if(value === 'true'){
        value = true
      }else if(value === 'false'){
        value = false
      }
    }


    if (mode === 'read') {
      if (value == null) {
        return
      }

      return value ? '是' : '否'
    }

    return (
        <Radio.Group value={value} onChange={onChange}>
          <Radio value={true}>是</Radio>
          <Radio value={false}>否</Radio>
          <Radio value={null}>未知</Radio>
        </Radio.Group>
    );
  }
}

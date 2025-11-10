/**
 * 类似下拉选择的功能，不过是横向平铺
 * 可以使用 url 或直接设置data
 */
import React from 'react';
import { Button, message } from 'antd';

import './index.less';
import {HttpUtil} from "../../../system";

/*
interface FlatMultipleSelectProps {
  url?: string,
  data?: DATA[]
  onChange: (value: any) => void;
  disabled: boolean;
  style: any
}

interface DATA {
  label: string,
  value: any
}
*/
export { FlatMultipleSelect as FieldFlatMultipleSelect };

export class FlatMultipleSelect extends React.Component {
  state = {
    data: [],
    fetching: false,
    searchText: null,
    value: [],
  };

  constructor(props) {
    super(props);
    if (props.data != null) {
      this.state.data = props.data;
    }
  }

  componentDidMount() {
    this.loadData();
  }

  loadData = (searchText = '') => {
    const { url } = this.props;
    if (url == null) {
      return;
    }

    this.setState({ fetching: true });

    HttpUtil.get(url, { searchText }).then(rs => {
      this.setState({ fetching: false });

      if (!(rs instanceof Array)) {
        message.error('返回结果的data字段应该为数组');
        return;
      }

      this.setState({ data: rs });
    });
  };
  onBtnClick = (v) => {
    let value = this.state.value;
    if (v == null) {
      // 所有
      value = [];
    } else {
      const value = this.state.value;
      const idx = value.indexOf(v);
      const exist = idx != -1;
      if (exist) {
        value.splice(idx, 1);
      } else {
        value.push(v);
      }
    }

    this.setState({ value: value });

    if (this.props.onChange) {
      this.props.onChange(value);
    }
  };

  render() {
    const value = this.state.value || [];
    const data = this.state.data;
    return (
      <div className="flat-select">
        <Button
          type={'text'}
          size={'small'}
          className={value.length == 0 ? 'checked' : ''}
          onClick={() => this.onBtnClick(null)}
        >
          所有
        </Button>

        {data.map((d) => (
          <Button
            key={d.value}
            type={'text'}
            size={'small'}
            className={value.indexOf(d.value) != -1 ? 'checked' : ''}
            onClick={() => this.onBtnClick(d.value)}
          >
            {d.label}
          </Button>
        ))}
      </div>
    );
  }
}

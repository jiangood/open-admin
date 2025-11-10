/**
 * 类似下拉选择的功能，不过是横向平铺
 * 可以使用 url 或直接设置data
 */
import React from 'react';
import { Button, message } from 'antd';

import './index.less';
import {http} from "../../../system";

/*
interface FlatMultipleSelectProps {
  url?: string,
  data?: DATA[]
  onChange?: (value: any) => void;
  disabled?: boolean;
  style?: any,
  showAll?: boolean,

  value?: any
}

interface DATA {
  label: string,
  value: any
}

 */
export { FlatSelect as FieldFlatSelect };
export class FlatSelect extends React.Component {
  state = {
    data: [],
    fetching: false,
    searchText: null,
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
    this.setState({ value: v });

    if (this.props.onChange) {
      this.props.onChange(v);
    }
  };

  render() {
    const value = this.props.value;
    const data = this.state.data;

    const showAll = this.props.showAll == null ? true : this.props.showAll;
    return (
      <div className="flat-select">
        {showAll && (
          <Button
            type={'text'}
            size={'small'}
            className={value == null ? 'checked' : ''}
            onClick={() => this.onBtnClick(null)}
          >
            所有
          </Button>
        )}

        {data.map((d) => (
          <Button
            key={d.value}
            type={'text'}
            size={'small'}
            className={value == d.value ? 'checked' : ''}
            onClick={() => this.onBtnClick(d.value)}
          >
            {d.label}
          </Button>
        ))}
      </div>
    );
  }
}

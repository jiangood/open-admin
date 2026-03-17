import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Select, Spin, message } from 'antd';
import { debounce } from 'lodash';
import { HttpUtils } from "../../utils";

export class FieldRemoteSelect extends React.Component {

    static defaultProps = {
        placeholder: '请搜索选择'
    }

    state = {
        dataLoading:true,
        data: {}
    }

    componentDidMount() {
        this.loadData(null);
    }

    loadData = (searchText) => {
        this.setState({dataLoading:true})
        const {url} = this.props;
        const params = {
            searchText,
            selected: this.props.value
        };

        HttpUtils.get(url,params).then(data => {
            this.setState({data})
        }).finally(() => {
            this.setState({dataLoading: false})
        })
    };
    handleSearch = searchText => {
        this.loadData(searchText)
    };

    render() {
        let {
            value,
            placeholder,
            ...rest
        } = this.props;

        return (
            <Select
                showSearch={{
                    filterOption: false,
                    onSearch: this.handleSearch,
                }}
                options={this.state.data}
                notFoundContent={this.state.dataLoading ? <Spin size="small"/> : '数据为空'}
                style={{width: '100%', minWidth: 200}}
                allowClear
                loading={this.state.dataLoading}
                value={value}
                {...rest}
            />
        );
    }
}





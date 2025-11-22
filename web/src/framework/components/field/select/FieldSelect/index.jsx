import {Button, Select, Spin} from 'antd';

import React from 'react';
import {HttpUtil} from "../../../../system";
import {ReloadOutlined} from "@ant-design/icons";
import {ObjUtil} from "../../../../utils";

/**
 * @deprecated
 */
export class FieldSelect extends React.Component {

    constructor(props) {
        super(props);
        console.error('FieldSelect即将弃用，请使用FRemoteSelect')
        ObjUtil.copyPropertyIfPresent(props, this.state)
        this.state.componentValue = this.convertInputToComponentValue(this.state.value)

        if (this.state.multiple) {
        }
    }

    state = {
        // 参数
        url: '',
        data: [],
        value: null,
        loading: false,
        searchText: '',
        showRefresh: false,
        placeholder: '请选择',

        multiple: false,
        valueType: 'primitive',


        // 内部状态
        componentValue: null
    }


    componentDidMount() {
        this.loadData({});
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        let {value} = this.props;
        // 表单主动设置value的情况
        if (value !== prevProps.value && this.state.value !== value) {
            const v = this.convertInputToComponentValue(value);
            this.setState({componentValue: v, value: v}, () => this.loadData({}))
        }
    }

    convertInputToComponentValue = value => {
        if (value != null) {
            if (this.state.multiple) {
                if (this.state.valueType === 'object') {
                    return value.map(v => v.id)
                }
                if (this.state.valueType === 'joined') {
                    return value.split(',')
                }
            } else {
                if (this.state.valueType === 'object') {
                    return value.id
                }
            }

        }

        return value
    };

    convertComponentValueToOutput = value => {
        if (value != null) {
            if (this.state.multiple) {
                if (this.state.valueType === 'object') {
                    return value.map(v => {
                        return {id: v}
                    })
                }
                if (this.state.valueType === 'joined') {
                    return value.join(',')
                }
            } else {
                if (this.state.valueType === 'object') {
                    return {id: value}
                }
            }
        }

        return value
    };


    handleChange = (value) => {
        this.setState({componentValue: value, loading: false});
        this.props.onChange?.(this.convertComponentValueToOutput(value));
    }


    handleSearch = (searchText) => {
        this.setState({searchText});
        this.loadData({searchText: searchText});
    };

    loadData = ({searchText}) => {
        const {value, url} = this.state;

        let selected = null;
        if (value != null) {
            const isArray = value instanceof Array
            if (isArray) {
                selected = value.join(',')
            } else {
                selected = value
            }
        }


        this.setState({loading: true});
        HttpUtil.get(url, {searchText, selected})
            .then(data => {
                this.setState({data});
                this.props.onLoadDataFinish?.(data);
            })
            .finally(() => {
                this.setState({loading: false});
            })
    };


    render() {
        console.log(this.props.disabled)
        return (
            <div style={{display: 'flex', alignItems: 'center'}}>
                <Select
                    {...this.props}
                    allowClear
                    showSearch
                    style={{width: '100%', minWidth: 200}}
                    notFoundContent={this.state.loading ? <Spin size="small"/> : null}
                    filterOption={false}
                    onSearch={this.handleSearch}
                    onChange={this.handleChange}
                    placeholder={this.state.placeholder}
                    value={this.state.componentValue}
                    options={this.state.data}
                    mode={this.state.multiple ? 'multiple' : undefined}
                >
                </Select>
                {this.state.showRefresh &&
                    <Button onClick={this.loadData} autoInsertSpace={false} icon={<ReloadOutlined/>}
                            size='small'/>}
            </div>
        );
    }
}

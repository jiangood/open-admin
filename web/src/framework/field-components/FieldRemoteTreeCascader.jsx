import {Cascader, message, Spin} from 'antd';

import React from 'react';
import {TreeUtil} from "../utils";
import {HttpUtil} from "../system";


export class FieldRemoteTreeCascader extends React.Component {

    state = {
        data: [],
        value: [],
        loading: false,
    };

    componentDidMount() {
        this.loadData();
    }


    loadData = async () => {
        const {url} = this.props;
        this.setState({loading: true});

        try {
            const list = await HttpUtil.get(url);
            this.setState({data: list});
        } catch (e) {
            console.log(e)
        } finally {
            this.setState({loading: false});
        }

    };

    handleChange = (arr) => {
        if (this.props.onChange) {
            this.props.onChange(arr[arr.length - 1]);
        }
    };

    render() {
        const {data, map, loading} = this.state;
        if (loading) {
            return <Spin/>;
        }
        let {value, ...restProps} = this.props;

        let arr = [];
        if (value != null) {
            arr = TreeUtil.getKeyList(data, value);
        }

        return (
            <Cascader
                options={data}
                onChange={this.handleChange}
                value={arr}
                fieldNames={{label: 'title', value: 'key'}}
                {...restProps}
            />
        );
    }
}

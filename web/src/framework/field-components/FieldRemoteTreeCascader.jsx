import {Cascader, message, Spin} from 'antd';

import React from 'react';
import {HttpUtils, TreeUtils} from "../utils";


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
            const list = await HttpUtils.get(url);
            this.setState({data: list});
        } catch (e) {
            console.log(e)
        } finally {
            this.setState({loading: false});
        }

    };



    render() {
        const {data} = this.state;
        if (this.state.loading) {
            return <Spin/>;
        }
        let {value, onChange,...rest} = this.props;

        let arr = [];
        if (value != null) {
            arr = TreeUtils.getKeyList(data, value);
        }

        return (
            <Cascader
                options={this.state.data}
                onChange={arr=>{
                    onChange(arr[arr.length - 1]);
                }}
                value={arr}
                fieldNames={{label: 'title', value: 'key'}}
                {...rest}
            />
        );
    }
}

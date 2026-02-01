import {Cascader, message, Spin} from 'antd';
import React from 'react';
import {HttpUtils, TreeUtils} from "../../utils";

/**
 * 远程树级联选择器, 类似select，但是树级联
 *
 * 注意，value为非数组形式，区别于cascader组件
 */
export class FieldRemoteTreeCascader extends React.Component {
    state = {
        data: [],
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
            console.log(e);
        } finally {
            this.setState({loading: false});
        }

    };

    render() {
        const {data} = this.state;
        if (this.state.loading) {
            return <Spin/>;
        }
        const {value, onChange, ...rest} = this.props;

        let arr = [];
        if (value != null) {
            arr = TreeUtils.getKeyList(data, value);
        }

        return (
            <Cascader
                options={this.state.data}
                onChange={arr => {
                    onChange && onChange(arr[arr.length - 1]);
                }}
                value={arr}
                fieldNames={{label: 'title', value: 'key'}}
                {...rest}
            />
        );
    }
}
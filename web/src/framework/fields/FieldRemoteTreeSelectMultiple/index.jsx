import {Spin, TreeSelect} from 'antd';
import React from 'react';
import {HttpUtils, StringUtils} from "../../utils";

export class FieldRemoteTreeSelectMultiple extends React.Component {
    static defaultProps = {
        treeDefaultExpandAll: true,
        style: {
            width: '100%',
            minWidth: 200,
        },
    };

    state = {
        data: [],
        loading: false,
        key: this.props.id,
    };

    componentDidMount() {
        this.loadData();
    }

    loadData = async () => {
        const {url} = this.props;
        this.setState({loading: true});
        try {
            const rs = await HttpUtils.get(url);
            this.setState({data: rs});
        } finally {
            this.setState({loading: false});
        }
    };

    render() {
        const {value, onChange, style, treeDefaultExpandAll} = this.props;
        const {data, loading} = this.state;

        if (loading) {
            return <Spin/>;
        }

        return (
            <TreeSelect
                style={style}
                allowClear={true}
                dropdownStyle={{maxHeight: 400, overflow: 'auto'}}
                treeData={data}
                showCheckedStrategy={TreeSelect.SHOW_ALL}
                value={value || undefined}
                onChange={onChange}
                multiple={true}
                filterTreeNode={(inputValue, treeNode) => {
                    const {title} = treeNode;
                    return StringUtils.contains(title, inputValue);
                }}
                treeLine={{showLeafIcon: true}}
                treeDefaultExpandAll={treeDefaultExpandAll}
            />
        );
    }
}
import React from "react";
import {Button, Select} from "antd";
import {ProTable} from "../../components";
import {HttpUtils} from "../../utils";

/**
 * 下拉表格
 *
 * 后端参考接口：
 */
export class FieldTableSelect extends React.Component {

    static defaultProps = {
        placeholder: '请搜索选择',
    };

    state = {
        open: false,
        label: '',
    };

    render() {
        return <Select popupRender={this.popupRender}
                       open={this.state.open}
                       onOpenChange={v => this.setState({open: v})}
                       style={{minWidth: 300}}
                       value={this.props.value}
                       labelRender={() => this.state.label}
                       popupMatchSelectWidth={900}
                       placeholder={this.props.placeholder}
        />;
    }

    popupRender = () => {
        return <ProTable
            columns={[...this.props.columns, {
                title: '操作',
                dataIndex: 'action',
                width: 100,
                render: (text, record) => {
                    return <Button
                        size='small'
                        type='primary'
                        onClick={() => {
                            this.setState({
                                label: record.name,
                                open: false
                            });
                            this.props.onChange && this.props.onChange(record.id);
                        }}>选择</Button>;
                }
            }]}
            showToolbarSearch
            request={(params) => {
                params.selected = this.props.value;
                return HttpUtils.get(this.props.url, params);
            }}>
        </ProTable>;
    };
}
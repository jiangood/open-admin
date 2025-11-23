import React from "react";
import {Button, Select} from "antd";
import {HttpUtil} from "../system";
import {ProTable} from "../components";

/**
 * 下拉表格
 */
export class FieldTableSelect extends React.Component {

    static defaultProps = {
        placeholder: '请搜索选择',
    }

    state = {
        open: false,
        label: '',
    }

    render() {
        return <Select popupRender={this.popupRender}
                       open={this.state.open}
                       onOpenChange={v => this.setState({open: v})}
                       style={{minWidth: 300}}
                       value={this.props.value}
                       labelRender={() => this.state.label}
                       popupMatchSelectWidth={900}
                       placeholder={this.props.placeholder}
        />
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
                            })
                            this.props.onChange(record.id)
                        }}>选择</Button>
                }
            }]}
            showToolbarSearch
            request={(params) => {
                params.selected = this.props.value
                return HttpUtil.get(this.props.url, params);
            }}>
        </ProTable>
    };
}

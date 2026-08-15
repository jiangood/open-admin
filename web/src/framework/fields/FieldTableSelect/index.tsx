import React from "react";
import {Button, Select} from "antd";
import type {TableColumnsType} from "antd";
import {ProTable} from "../../components";
import {HttpClient} from "../../utils";
import type {FieldProps} from '../types';

export interface FieldTableSelectProps extends FieldProps<string> {
    /** 数据加载地址 */
    url: string;
    /** 表格列配置（操作列由组件自动追加） */
    columns: TableColumnsType<Record<string, unknown>>;
    /** 占位文本，默认 请搜索选择 */
    placeholder?: string;
}

interface FieldTableSelectState {
    open: boolean;
    label: string;
}

/**
 * 下拉表格
 *
 * 后端参考接口：
 */
export class FieldTableSelect extends React.Component<FieldTableSelectProps, FieldTableSelectState> {

    static readonly defaultProps = {
        placeholder: '请搜索选择',
    };

    state: FieldTableSelectState = {
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
                            this.props.onChange?.(record.id);
                        }}>选择</Button>;
                }
            }]}
            request={(params, success, error) => {
                params.selected = this.props.value;
                return HttpClient.get(this.props.url, params, success, error);
            }}>
        </ProTable>;
    };
}
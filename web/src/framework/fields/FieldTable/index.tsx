import {Button, Input, Table} from 'antd'
import React from 'react'
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import type {FieldProps} from '../types';
import './styles.less'

/**
 * 可编辑表格的列配置。
 * render 返回的组件元素会被注入 value / onChange，用于编辑单元格。
 */
export interface FieldTableColumn {
    /** 列标题 */
    title?: React.ReactNode;
    /** 字段名 */
    dataIndex?: string;
    /** 列宽 */
    width?: number | string;
    /** 自定义渲染（返回的组件会被注入 value/onChange） */
    render?: (value: any, record: Record<string, any>, index: number) => React.ReactElement;
    /** 其余 antd Table 列属性 */
    [key: string]: any;
}

export interface FieldTableProps extends FieldProps<Record<string, any>[]> {
    /** 列配置（操作列由组件自动追加） */
    columns: FieldTableColumn[];
    /** 容器样式 */
    style?: React.CSSProperties;
}

interface FieldTableState {
    dataSource: Record<string, any>[];
}

/**
 * 可编辑表格
 */
export class FieldTable extends React.Component<FieldTableProps, FieldTableState> {

    columns: FieldTableColumn[] = [];

    constructor(props: FieldTableProps) {
        super(props);

        this.columns = this.props.columns.map(col => {
            const newCol = { ...col };
            const origRender = newCol.render;

            if (origRender) {
                newCol.render = (v, record, index) => {
                    const cmp = origRender(v, record, index);
                    return React.createElement(cmp.type, {
                        ...cmp.props,
                        value: v,
                        onChange: (e) => {
                            this.onCellChange(index, newCol.dataIndex, e);
                        }
                    });
                };
            } else {
                newCol.render = (v, record, index) => {
                    return <Input value={v} onChange={(e) => this.onCellChange(index, newCol.dataIndex, e)}/>;
                };
            }

            return newCol;
        });

        this.columns.push({
            title: '操作',
            render: (v, record) => {
                return <Button icon={<DeleteOutlined/>} title='删除' size='small' shape={'circle'}
                               onClick={() => this.remove(record)}></Button>;
            }
        });

        this.state = {
            dataSource: this.props.value || []
        };

    }

    state = {
        dataSource: []
    };

    onCellChange = (index: number, dataIndex: string | undefined, e: any) => {
        const {dataSource} = this.state;
        const row = dataSource[index];

        let v = e;
        if (e != null && typeof e === 'object' && 'target' in e) {
            v = e.target.value;
        }

        const next = [...dataSource];
        next[index] = {...row, [dataIndex as string]: v};
        this.setState({dataSource: next}, this.notifyParent);
    };

    add = () => {
        let {dataSource} = this.state;
        dataSource = [...dataSource, {}];
        this.setState({dataSource}, this.notifyParent);
    };

    remove = (record: Record<string, any>) => {
        const {dataSource} = this.state;
        this.setState({dataSource: dataSource.filter(item => item !== record)}, this.notifyParent);
    };

    notifyParent = () => {
        const {dataSource} = this.state;
        this.props.onChange && this.props.onChange(dataSource);
    };

    render() {
        return <div className='edit-table' style={this.props.style}>
            <Table columns={this.columns}
                   dataSource={this.state.dataSource}
                   size='small'
                   footer={() => <Button type='dashed'
                                         icon={<PlusOutlined/>}
                                         onClick={this.add}>增加一行
                   </Button>}
                   pagination={false}
            >

            </Table>


        </div>;
    }
}
import {Button, Input, Table} from 'antd'
import React from 'react'
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import './FieldTable.less'
import {ArrUtils} from "../utils";

export class FieldTable extends React.Component {


    columns = []


    constructor(props) {
        super(props);

        this.columns = this.props.columns.map(col => {
            if (col.render == null) {
                col.render = (v, record, index) => {
                    return <Input value={v} onChange={(e) => this.onCellChange(index, col.dataIndex, e)}/>
                }
            } else {
                if (!col._oldRender) {
                    col._oldRender = col.render
                    col.render = (v, record, index) => {
                        const cmp = col._oldRender(v, record, index)
                        return React.createElement(cmp.type,
                            {
                                ...cmp.props,
                                value: v,
                                onChange: (e) => {
                                    this.onCellChange(index, col.dataIndex, e);
                                }
                            })
                    }
                }
            }
            return col
        })

        this.columns.push({
            title: '操作',
            render: (v, record) => {
                return <Button icon={<DeleteOutlined/>} title='删除' size='small' shape={'circle'}
                               onClick={() => this.remove(record)}></Button>
            }
        })

        if (this.props.value != null) {
            this.state.dataSource = this.props.value
        }

    }

    state = {
        dataSource: []
    }


    onCellChange = (index, dataIndex, e) => {
        let {dataSource} = this.state
        let row = dataSource[index]

        let v = e;
        if (e != null && e.hasOwnProperty('target')) {
            v = e.target.value;
        }


        row[dataIndex] = v

        dataSource = [...dataSource]
        this.setState({dataSource}, this.notifyParent)
    }

    add = () => {
        let {dataSource} = this.state
        dataSource = [...dataSource, {}];
        this.setState({dataSource}, this.notifyParent)
    };
    remove = (record) => {
        let {dataSource} = this.state
        ArrUtils.remove(dataSource, record)
        this.setState({dataSource: [...dataSource]}, this.notifyParent)
    };

    notifyParent() {
        let {dataSource} = this.state
        this.props.onChange(dataSource)
    }

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


        </div>
    }
}

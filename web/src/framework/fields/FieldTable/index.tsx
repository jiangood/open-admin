import {Button, Input, Table} from 'antd'
import React from 'react'
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import './styles.less'

/**
 * 可编辑表格
 */
export class FieldTable extends React.Component {

    columns = [];

    constructor(props) {
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

    onCellChange = (index, dataIndex, e) => {
        let {dataSource} = this.state;
        const row = dataSource[index];

        let v = e;
        if (e != null && typeof e === 'object' && 'target' in e) {
            v = e.target.value;
        }

        row[dataIndex] = v;

        dataSource = [...dataSource];
        this.setState({dataSource}, this.notifyParent);
    };

    add = () => {
        let {dataSource} = this.state;
        dataSource = [...dataSource, {}];
        this.setState({dataSource}, this.notifyParent);
    };

    remove = (record) => {
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
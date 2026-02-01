import React from 'react'
import {HttpUtils, ProTable} from "../../../framework";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false,
        selectedRowKeys: []
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [


        {
            title: '编码',
            dataIndex: 'code',


        },
        {
            title: '文本',
            dataIndex: 'text',
        },


    ]


    render() {
        return <>
            <ProTable
                actionRef={this.tableRef}

                request={(params) => HttpUtils.get('admin/sysDict/page', params)}
                columns={this.columns}
                rowKey='id'

                rowSelection={{
                    type: 'radio',
                    selectedRowKeys: this.state.selectedRowKeys,
                    onChange: (selectedRowKeys, selectedRows) => {
                        this.setState({selectedRowKeys: selectedRowKeys})
                        this.props.onChange(selectedRowKeys[0])
                    }
                }}
                onRow={(record) => ({
                    onClick: () => {
                        this.setState({selectedRowKeys: [record.id]})
                        this.props.onChange(record.id)
                    }
                })}
            />


        </>


    }
}




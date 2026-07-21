import React from "react";
import {Button, Form, Table} from 'antd';

import {StringUtils} from "../../utils";

import './index.less'


export class ProTable extends React.Component {


    state = {
        selectedRowKeys: [],
        selectedRows: [],

        tableSize: 'small',

        loading: true,
        params: {},
        dataSource: [],


        total: 0,
        current: 1, // 当前页
        pageSize: 10,

        sorter: {
            field: undefined, // 字段
            order: undefined, // 排序 ascend, descend
        },

        // 服务端返回的一些额外数据
        extData: {
            // 总结栏
            summary: null,
        },

        scrollY: null
    }


    constructor(props) {
        super(props);
        if (props.defaultPageSize) {
            this.state.pageSize = props.defaultPageSize
        }
        this.id = StringUtils.random(32)
    }

    formRef = React.createRef()

    componentDidMount() {
        this.loadData()
        if (this.props.actionRef) {
            this.props.actionRef.current = {
                reload: () => this.loadData()
            }
        }

        const scrollY = this.props.scrollY;
        if (scrollY) {
            this.setState({scrollY: scrollY})
        }
    }

    loadData = () => {
        const {request} = this.props
        const params = {...this.state.params}
        params.size = this.state.pageSize
        params.page = this.state.current

        const {sorter} = this.state

        const {field, order} = sorter
        if (field) {
            params.sort = field + "," + (order === 'ascend' ? 'asc' : 'desc')
        }


        this.setState({loading: true})
        request(params).then(rs => {
            const {content, totalElements, size,extData} = rs;


            this.setState({dataSource: content, total: parseInt(totalElements),pageSize:size})
            if (extData) {
                this.setState({extData})
            }
            this.updateSelectedRows(content)

        }).finally(() => {
            this.setState({loading: false})
        })
    }


    // 数据重新加载后，更新toolbar需要的已选择数据行
    updateSelectedRows = list => {
        const {rowKey = "id"} = this.props
        const {selectedRows} = this.state
        for (let i = 0; i < selectedRows.length; i++) {
            for (let newItem of list) {
                const oldItem = selectedRows[i];
                if (oldItem[rowKey] === newItem[rowKey]) {
                    selectedRows[i] = newItem;
                    break
                }
            }
        }

        this.setState({selectedRows: [...selectedRows]})
    };


    render() {
        const {
            actionRef,
            toolBarRender,
            columns,
            rowSelection,
            rowKey = "id",
        } = this.props


        return <div className={'oa-pro-table '} id={this.id}>
            {this.renderForm()}
            <div className="pro-table-wrapper">
                {toolBarRender && <div className="pro-table-toolbar">
                    <div className="pro-table-toolbar-left">
                        {this.getToolBarRenderNode(toolBarRender)}
                    </div>
                </div>}


                <Table
                    loading={this.state.loading}
                    columns={columns}
                    dataSource={this.state.dataSource}
                    rowKey={rowKey}
                    size={this.state.tableSize}
                    rowSelection={this.getRowSelectionProps(rowSelection)}
                    scroll={{x: 'max-content', y: this.state.scrollY}}
                    pagination={{
                        showSizeChanger: true,
                        total: this.state.total,
                        pageSize: this.state.pageSize,
                        current: this.state.current,
                        pageSizeOptions: [10, 20, 50, 100, 500, 1000, 5000],
                        showTotal: (total) => `共 ${total} 条`
                    }}

                    onChange={(pagination, filters, sorter, extra) => {
                        this.setState({
                            current: pagination.current,
                            pageSize: pagination.pageSize,
                            sorter
                        }, this.loadData)
                    }}

                    footer={this.state.extData.summary ? () => this.state.extData.summary : null}
                    bordered={this.props.bordered ?? false}
                />
            </div>
        </div>

    }


    renderForm = () => {
        if (!this.props.children) return

        return (
            <Form
                className="filter-bar"
                style={{gridTemplateColumns: `repeat(${this.props.searchColumns ?? 4}, 1fr)`}}
                onFinish={(values) => this.onSearch(values)}
                ref={(instance) => {
                    this.formRef.current = instance;
                    if (this.props.formRef) this.props.formRef.current = instance;
                }}
            >
                {this.props.children}
                <div className="filter-actions">
                    <Button type='primary' htmlType="submit">查询</Button>
                    <Button onClick={() => {
                        this.formRef.current?.resetFields();
                        this.formRef.current?.submit();
                    }}>重置</Button>
                </div>
            </Form>
        )
    };

    getToolBarRenderNode(toolBarRender) {
        if (!toolBarRender) {
            return
        }
        const {selectedRows, selectedRowKeys, params} = this.state;
        return toolBarRender(params, {
            selectedRows: selectedRows,
            selectedRowKeys: selectedRowKeys,
        });
    }

    getRowSelectionProps = rowSelection => {
        if (rowSelection == null || rowSelection === false) {
            return null
        }
        if (rowSelection === true) {
            rowSelection = {}
        }
        const {type, onChange: inputOnChange} = rowSelection


        return {
            type,
            onChange: (selectedRowKeys, selectedRows) => {
                this.setState({selectedRowKeys, selectedRows})
                if (inputOnChange) {
                    inputOnChange(selectedRowKeys, selectedRows)
                }
            },
            selectedRowKeys: this.state.selectedRowKeys
        };
    };

    onSearch = (values) => {
        this.setState({params: values, current: 1}, this.loadData)
    }

    changeFormValues = (values) => {
        if (this.formRef.current) {
            this.formRef.current.resetFields()
            this.formRef.current.setFieldsValue(values)
            this.formRef.current.submit()
        }
    }

}








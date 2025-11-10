import {Button, Card, Divider, Form, Input, Table} from 'antd';
import Toolbar from './components/ToolBar';
import React from "react";
import './index.less'
import {StrUtil} from "@tmgg/tmgg-commons-lang";
import {SearchOutlined} from "@ant-design/icons";


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
        this.id = StrUtil.random(32)
        this.showSearchForm = this.props.children && this.props.children.length > 0
        this.showToolbarSearch = !this.showSearchForm
    }

    formRef = React.createRef()

    componentDidMount() {
        this.loadData()
        if (this.props.actionRef) {
            this.props.actionRef.current = {
                reload: () => this.loadData()
            }
        }

        let scrollY = this.props.scrollY;
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
            const {content, totalElements, extData} = rs;

            this.setState({dataSource: content, total: parseInt(totalElements)})
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
                let oldItem = selectedRows[i];
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
            toolbarOptions,
        } = this.props


        return <div className={'tmgg-pro-table '} id={this.id}>

            {this.renderForm()}


                {toolbarOptions !== false && <Toolbar
                    actionRef={actionRef}
                    toolBarRender={this.getToolBarRenderNode(toolBarRender)}

                    onRefresh={() => this.loadData()}
                    onSearch={this.onSearch}
                    loading={this.state.loading}
                    params={this.state.params}
                    showSearch={this.showToolbarSearch}
                    changeFormValues={this.changeFormValues}
                />}


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
                    bordered={this.props.bordered}
                />
        </div>

    }


    renderForm = () => {
        if (!this.props.children) {
            return
        }
        if (this.props.searchFormItemsRender) {
            throw new Error('不再支持 searchFormItemsRender，请直接放到ProTable的子节点')
        }

        return <div style={{marginBottom: '16px'}}>
                <Form
                    layout="inline"
                    onFinish={(values) => this.onSearch(values)}
                    ref={(instance) => {
                        this.formRef.current = instance;
                        if (this.props.formRef != null) {
                            this.props.formRef.current = instance;
                        }
                    }}
                    style={{gap: '8px 0px'}}
                    labelCol={{flex: '70px'}}
                >

                    {this.props.children}

                    <Form.Item style={{marginLeft: this.props.children.length > 2 ? 'auto' : 'inherit'}}>
                        <Button type='primary' htmlType="submit" icon={<SearchOutlined/>}> 查询
                        </Button>
                    </Form.Item>
                </Form>
        </div>;
    };

    getToolBarRenderNode(toolBarRender) {
        if (!toolBarRender) {
            return
        }
        let {selectedRows, selectedRowKeys, params} = this.state;
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
        let {type, onChange: inputOnChange} = rowSelection


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








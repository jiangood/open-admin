import React from "react";
import {Button, Form, Table} from 'antd';
import type {FormInstance, TableProps} from 'antd';

import {StringUtils, type AjaxBody} from "../../utils";

import './index.less'

/** 通过 actionRef 暴露的表格操作 */
export interface ProTableActionRef {
    reload: () => void;
    clearSelection: () => void;
}

/** request 返回结构（Spring Data Page 序列化 + 扩展数据） */
export interface ProTableRequestResult<T = unknown> {
    content: T[];
    totalElements: number | string;
    size: number;
    extData?: {
        summary?: React.ReactNode;
        [key: string]: unknown;
    };
}

export interface ProTableProps<T = unknown> {
    /**
     * 数据请求（Promise 式），框架自动注入 page/size/sort 参数。
     * 页面实现直接返回 HttpClient 的 Promise：request = (params) => HttpClient.get(url, params)
     * ProTable 会自动解包 AjaxResult 取 data；手动返回分页结构 {content, totalElements, extData} 亦可。
     * 失败时 HttpClient 默认自动弹错，ProTable 内部静默复位 loading。
     */
    request: (params: Record<string, unknown>) => Promise<ProTableRequestResult<T> | AjaxBody<ProTableRequestResult<T>>>;
    /** antd Table 列定义 */
    columns: TableProps<T>['columns'];
    /** 获取表格操作句柄（reload） */
    actionRef?: React.RefObject<ProTableActionRef | undefined>;
    /** 获取搜索表单实例 */
    formRef?: React.RefObject<FormInstance | undefined>;
    /** 工具栏渲染，参数为当前搜索值与行选择状态 */
    toolBarRender?: (params: Record<string, unknown>, selection: {
        selectedRows: T[];
        selectedRowKeys: React.Key[];
    }) => React.ReactNode;
    rowKey?: string;
    /** 行选择：true 为默认 checkbox，对象可覆盖 type/onChange */
    rowSelection?: boolean | {
        type?: 'checkbox' | 'radio';
        onChange?: (selectedRowKeys: React.Key[], selectedRows: T[]) => void;
    };
    defaultPageSize?: number;
    /** 搜索栏每行列数，默认 4 */
    searchFormCols?: number;
    /** 搜索表单渲染函数，返回 Form.Item 列表 */
    searchFormRender?: () => React.ReactNode;
    scrollY?: number | string;
    bordered?: boolean;
    /** 树形数据模式，关闭分页且不传 page/size 参数 */
    treeMode?: boolean;
}

interface ProTableState<T = unknown> {
    selectedRowKeys: React.Key[];
    selectedRows: T[];
    tableSize: 'small' | 'middle' | 'large';
    loading: boolean;
    params: Record<string, unknown>;
    dataSource: T[];
    total: number;
    current: number;
    pageSize: number;
    sorter: {
        field?: string;
        order?: 'ascend' | 'descend' | null;
    };
    extData: {
        summary?: React.ReactNode;
        [key: string]: unknown;
    };
    scrollY: number | string | null;
}

export class ProTable<T = unknown> extends React.Component<ProTableProps<T>, ProTableState<T>> {


    state: ProTableState<T> = {
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


    id: string;
    formRef: React.RefObject<FormInstance | null> = React.createRef();

    constructor(props: ProTableProps<T>) {
        super(props);
        if (props.defaultPageSize) {
            this.state.pageSize = props.defaultPageSize
        }
        this.id = StringUtils.random(32)
    }

    componentDidMount() {
        this.loadData()
        if (this.props.actionRef) {
            this.props.actionRef.current = {
                reload: () => this.loadData(),
                clearSelection: () => this.setState({selectedRowKeys: [], selectedRows: []})
            }
        }

        const scrollY = this.props.scrollY;
        if (scrollY) {
            this.setState({scrollY: scrollY})
        }
    }

    loadData = () => {
        const {request, treeMode} = this.props
        const params = {...this.state.params}
        if (!treeMode) {
            params.size = this.state.pageSize
            params.page = this.state.current
        }

        const {sorter} = this.state

        const {field, order} = sorter
        if (field && order) {
            params.sort = field + "," + (order === 'ascend' ? 'asc' : 'desc')
        }


        this.setState({loading: true})
        request(params).then((rs: ProTableRequestResult<T> | AjaxBody<ProTableRequestResult<T>>) => {
            const pageData = this.normalizePageResult(rs);
            const {content, totalElements, size, extData} = pageData;

            this.setState({dataSource: content, total: Number(totalElements), pageSize: size})
            if (extData) {
                this.setState({extData})
            }
            this.updateSelectedRows(content)
            this.setState({loading: false})
        }).catch(() => {
            // 错误提示已由 HttpClient 默认弹出，此处仅复位 loading
            this.setState({loading: false})
        })
    }

    /**
     * 归一化 request 返回值：AjaxBody 取 data，已是分页结构（含 content/totalElements）原样使用，其余透传。
     */
    private normalizePageResult(rs: ProTableRequestResult<T> | AjaxBody<ProTableRequestResult<T>>): ProTableRequestResult<T> {
        if (rs && typeof rs === 'object' && 'content' in rs && 'totalElements' in rs) {
            return rs as ProTableRequestResult<T>;
        }
        if (rs && typeof rs === 'object' && 'success' in rs) {
            return (rs as AjaxBody<ProTableRequestResult<T>>).data;
        }
        return rs as ProTableRequestResult<T>;
    }


    // 数据重新加载后，更新toolbar需要的已选择数据行
    updateSelectedRows = list => {
        const {rowKey = "id"} = this.props
        const {selectedRows} = this.state
        const rowMap = new Map(list.map(item => [item[rowKey], item]))
        const updated = selectedRows.map(old => rowMap.get(old[rowKey]) || old)

        this.setState({selectedRows: updated})
    };


    render() {
        const {
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
                    pagination={this.props.treeMode ? false : {
                        showSizeChanger: true,
                        total: this.state.total,
                        pageSize: this.state.pageSize,
                        current: this.state.current,
                        pageSizeOptions: [10, 20, 50, 100, 500, 1000, 5000],
                        showTotal: (total) => `共 ${total} 条`
                    }}

                    onChange={(pagination, filters, sorter) => {
                        if (this.props.treeMode) {
                            this.setState({sorter}, this.loadData)
                        } else {
                            this.setState({
                                current: pagination.current,
                                pageSize: pagination.pageSize,
                                sorter
                            }, this.loadData)
                        }
                    }}

                    footer={this.state.extData.summary ? () => this.state.extData.summary : null}
                    bordered={this.props.bordered ?? false}
                />
            </div>
        </div>

    }


    renderForm = () => {
        if (this.props.children) {
            throw new Error('[ProTable] children 已废弃，请使用 searchFormRender prop 替代')
        }
        const searchContent = this.props.searchFormRender?.()
        if (!searchContent) return

        return (
            <Form
                className="filter-bar"
                style={{gridTemplateColumns: `repeat(${this.props.searchFormCols ?? 4}, 1fr)`}}
                onFinish={(values) => this.onSearch(values)}
                ref={(instance) => {
                    this.formRef.current = instance;
                    if (this.props.formRef) this.props.formRef.current = instance;
                }}
            >
                {searchContent}
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

    getToolBarRenderNode(toolBarRender: NonNullable<ProTableProps<T>['toolBarRender']>) {
        if (!toolBarRender) {
            return
        }
        const {selectedRows, selectedRowKeys, params} = this.state;
        return toolBarRender(params, {
            selectedRows: selectedRows,
            selectedRowKeys: selectedRowKeys,
        });
    }

    getRowSelectionProps = (rowSelection: ProTableProps<T>['rowSelection']) => {
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

    onSearch = (values: Record<string, unknown>) => {
        this.setState({params: values, current: 1, sorter: {}}, this.loadData)
    }

    changeFormValues = (values: Record<string, unknown>) => { // NOSONAR: ref 暴露给父组件/业务项目调用的公共 API
        if (this.formRef.current) {
            this.formRef.current.resetFields()
            this.formRef.current.setFieldsValue(values)
            this.formRef.current.submit()
        }
    }

}








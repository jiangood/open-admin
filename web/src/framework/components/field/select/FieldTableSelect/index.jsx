import React from "react";
import {Input, Select, Table, Typography} from "antd";
import {SearchOutlined} from "@ant-design/icons";
import {HttpUtil} from "../../../../system";

/**
 * 下拉表格
 */
export class FieldTableSelect extends React.Component {

    state = {
        loading: false,
        columns: [],
        dataSource: [],

        open: false,
        searchText: null,

        value: null,
        label: null,

        // 分页参数
        totalElements: 0,
        current: 1,
        pageSize: 10,
        sorter: null

    }


    componentDidMount() {
        let value = this.props.value;
        this.state.value = value
        // 首次加载时，将默认的那条数据也下载下来，方便显示label
        this.initData(value);

        this.loadData()
    }

    initData(value) {
        if (value != null) {
            let selected = [value];
            HttpUtil.post(this.props.url, {selected}, {size:1})
                .then(({ dataSource}) => {
                const record = dataSource[0]
                if (record) {
                    this.setState({label: record[this.props.labelKey]})
                }
            })
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        let {value} = this.props;
        // 表单主动设置value的情况
        if (value !== prevProps.value && this.state.value !== value) {
            this.setState({value: value, label: null}, this.loadData)
        }
    }

    loadData = searchText => {
        this.setState({loading: true})

        const params = {
            size: this.state.pageSize,
            page: this.state.current,
            searchText,
        }


        if(this.state.sorter){
            const {field, order} =  this.state.sorter
            if (field) {
                params.sort = field + "," + (order === 'ascend' ? 'asc' : 'desc')
            }
        }


        HttpUtil.get(this.props.url, params).then(({columns, dataSource, totalElements}) => {
            this.setState({columns, dataSource, totalElements: parseInt(totalElements)})
        }).finally(() => {
            this.setState({loading: false})
        })
    };


    onSearch = searchText => {
        this.setState({searchText})
        this.loadData(searchText)
    };

    render() {
        return <>
            <Select popupRender={this.popupRender} loading={this.state.loading}
                    open={this.state.open}
                    onOpenChange={visible => this.setState({open: visible})}
                    style={{minWidth: 300}}
                    value={this.state.value}
                    labelRender={() => this.state.label}
                    popupMatchSelectWidth={900}
                    placeholder={this.props.placeholder || '请选择'}
            />
        </>
    }


    popupRender = () => {
        return <div>
            <div style={{padding: 8, display: 'flex', justifyContent: 'end'}}>
                <Input
                    placeholder="搜索..."
                    prefix={<SearchOutlined/>}
                    value={this.state.searchText}
                    onChange={(e) => this.onSearch(e.target.value)}
                    style={{maxWidth: 280}}
                />
            </div>
            <Table dataSource={this.state.dataSource} loading={this.state.loading}
                   columns={this.state.columns}
                   size='small'
                   bordered
                   rowKey='id'
                   onRow={(record) => {
                       return {
                           onDoubleClick: (event) => {
                               let value = record.id;
                               let label = record[this.props.labelKey]

                               this.setState({label, value,  open: false})
                               this.props.onChange?.(value)
                           },
                       }
                   }}

                   pagination={{
                       total: this.state.totalElements,
                       pageSize: this.state.pageSize,
                       current: this.state.current,
                       showTotal: (total) => `共 ${total} 条`
                   }}

                   onChange={(pagination, filters, sorter, extra) => {
                       this.setState({
                           current: pagination.current,
                           pageSize: pagination.pageSize,
                           sorter
                       }, this.loadData)
                   }}
                   summary={()=><Typography.Text>提示：双击选择数据</Typography.Text>}

            ></Table>

        </div>
    };
}

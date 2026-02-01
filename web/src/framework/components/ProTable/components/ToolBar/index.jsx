/**
 * 工具栏
 */
import {HistoryOutlined, ReloadOutlined} from '@ant-design/icons';
import {Button, Input, message, Modal, Table} from 'antd';
import React from 'react';
import './index.less';
import {DateUtils, PageUtils, StorageUtils} from "../../../../utils";

export default class Toolbar extends React.Component {

    state = {
        // 查询历史的模态框
        historyOpen: false
    }

    render = () => {
        const {
            onRefresh,
            toolBarRender,
            loading,
            showSearch
        } = this.props;

        return <div className='pro-table-toolbar'>

            <div className='pro-table-toolbar-left'>
                {showSearch && <Input.Search placeholder='搜索...'
                                             allowClear
                                             onChange={(e) => {
                                                 this.props.onSearch({searchText: e.target.value})
                                             }}
                />}
            </div>

            <div className='pro-table-toolbar-option'>
                {toolBarRender}
                <Button title='刷新' size='small' icon={<ReloadOutlined/>} onClick={onRefresh} loading={loading}/>
                <Button title='查询历史' size='small' icon={<HistoryOutlined/>} onClick={this.onClickHistory}/>
            </div>

            {this.renderHistory()}
        </div>
    };


    renderHistory() {
        const {params} = this.props
        const list = StorageUtils.get(this.getParamKey()) || []

        const dataSource = [{params, time: '当前'}, ...list]

        return <Modal title='查询方案'
                      width={800}
                      open={this.state.historyOpen}
                      onCancel={() => this.setState({historyOpen: false})}
                      footer={null}
                      destroyOnHidden
        >

            <Table
                dataSource={dataSource}
                pagination={false}
                rowKey='time'
                columns={[
                    {
                        dataIndex: 'time', title: '时间'
                    },
                    {
                        dataIndex: 'params', title: '参数',
                        render(v) {
                            return JSON.stringify(v)
                        }
                    }, {
                        dataIndex: 'option', title: '-',
                        render: (v, record) => {
                            if (record.time === '当前') {
                                return <Button onClick={this.onSaveHistory}>保存</Button>
                            } else {
                                return <Button type='primary' onClick={() => this.onApply(record.params)}>使用</Button>
                            }
                        }
                    }
                ]}></Table>


        </Modal>;
    }

    onClickHistory = () => {

        this.setState({historyOpen: true})
    }

    getParamKey() {
        return 'query-params-' + PageUtils.currentPathname();
    }

    onSaveHistory = () => {
        const {params} = this.props
        const keys = Object.keys(params)
        if (keys.length === 0) {
            message.error('查询参数为空，无法保存')
            return
        }
        const list = StorageUtils.get(this.getParamKey()) || []
        let data = {time: DateUtils.now(), params};
        list.unshift(data)
        if (list.length > 5) {
            list.pop()
        }
        StorageUtils.set(this.getParamKey(), list)
        message.success('保存成功')
        this.setState({historyOpen: false})
    }

    onApply = (params) => {
        this.props.changeFormValues(params)
        this.setState({historyOpen: false})
    }
}



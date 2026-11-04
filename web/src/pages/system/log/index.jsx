import React from 'react';
import {Button, Descriptions, Drawer, Form, Input, Tag, Typography} from "antd";
import {ViewText, FieldDateRange, HttpClient, getMessageApi, Page, PageUtils, ProTable} from "../../../framework";

export default class LogPage extends React.Component {

    state = {
        detailRecord: null,
    };

    columns = [
        {
            title: '操作者',
            dataIndex: 'username',
            width: 120,
        },
        {
            title: '操作',
            dataIndex: 'operation',
            width: 160,
        },
        {
            title: '操作详情',
            dataIndex: 'detail',
            render: (v) => v ? <ViewText value={v} ellipsis={true} maxLength={40}/> : '-',
        },
        {
            title: 'IP',
            dataIndex: 'ip',
            width: 140,
        },
        {
            title: '时间',
            dataIndex: 'operationTime',
            sorter: true,
            width: 180,
        },
        {
            title: '结果',
            dataIndex: 'success',
            width: 80,
            render(v) {
                return <Tag color={v ? 'green' : 'red'}>{v ? '成功' : '失败'}</Tag>
            }
        },
        {
            title: '详情',
            dataIndex: 'option',
            width: 80,
            fixed: 'right',
            render: (_, record) => (
                <Button type='link' size='small'
                        onClick={() => this.setState({detailRecord: record})}>详情</Button>
            ),
        },
    ];


    getDefaultParams = () => {
        const {userId, username, ip} = PageUtils.currentParams();
        const params = {};
        if (userId) params.userId = userId;
        if (username) params.username = username;
        if (ip) params.ip = ip;
        return Object.keys(params).length ? params : undefined;
    };

    copyParams = (text) => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text)
                .then(() => getMessageApi().success('已复制'))
                .catch(() => getMessageApi().error('复制失败'))
        } else {
            getMessageApi().warning('当前浏览器不支持剪贴板')
        }
    };

    renderDetailDrawer = () => {
        const record = this.state.detailRecord;
        return (
            <Drawer title='操作日志详情'
                    width={720}
                    open={!!record}
                    onClose={() => this.setState({detailRecord: null})}>
                {record && <>
                    <Descriptions size='small' column={1} bordered>
                        <Descriptions.Item label='操作'>{record.operation || '-'}</Descriptions.Item>
                        <Descriptions.Item label='操作详情'>{record.detail || '-'}</Descriptions.Item>
                        <Descriptions.Item label='操作者'>{record.username || '-'}</Descriptions.Item>
                        <Descriptions.Item label='时间'>{record.operationTime || '-'}</Descriptions.Item>
                        <Descriptions.Item label='IP'>{record.ip || '-'}</Descriptions.Item>
                        <Descriptions.Item label='耗时'>
                            {record.duration == null ? '-' : `${record.duration} ms`}
                        </Descriptions.Item>
                        <Descriptions.Item label='结果'>
                            <Tag color={record.success ? 'green' : 'red'}>{record.success ? '成功' : '失败'}</Tag>
                        </Descriptions.Item>
                        {!record.success && record.error && (
                            <Descriptions.Item label='错误消息'>
                                <Typography.Text type='danger'>{record.error}</Typography.Text>
                            </Descriptions.Item>
                        )}
                    </Descriptions>

                    <div style={{marginTop: 16}}>
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            marginBottom: 8,
                        }}>
                            <Typography.Text strong>请求参数</Typography.Text>
                            {record.params && (
                                <Button size='small' onClick={() => this.copyParams(record.params)}>复制</Button>
                            )}
                        </div>
                        {record.params
                            ? <pre style={{
                                maxHeight: 420,
                                overflow: 'auto',
                                margin: 0,
                                padding: 12,
                                borderRadius: 4,
                                background: 'var(--oa-color-fill, #f6f8fa)',
                                border: '1px solid var(--oa-color-border, #e8e8e8)',
                                whiteSpace: 'pre-wrap',
                                wordBreak: 'break-all',
                            }}>{record.params}</pre>
                            : <Typography.Text type='secondary'>
                                无请求参数（该接口未使用 @RequestBody，或请求体为空）
                            </Typography.Text>}
                    </div>
                </>}
            </Drawer>
        );
    };

    render() {
        return <Page>
            <ProTable
                rowKey='id'
                defaultParams={this.getDefaultParams()}
                request={(params) => HttpClient.get('admin/sys-log/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item label='操作' name='operation'>
                            <Input allowClear/>
                        </Form.Item>
                        <Form.Item label='操作详情' name='detail'>
                            <Input allowClear placeholder='按详情模糊搜索'/>
                        </Form.Item>
                        <Form.Item label='操作者' name='username'>
                            <Input allowClear/>
                        </Form.Item>
                        <Form.Item label='IP' name='ip'>
                            <Input allowClear/>
                        </Form.Item>
                        <Form.Item label='时间' name='dateRange'>
                            <FieldDateRange/>
                        </Form.Item>
                    </>
                )}
            />
            {this.renderDetailDrawer()}
        </Page>
    }


}

import React from 'react';
import {Button, Descriptions, Drawer, Form, Input, Modal, Popconfirm, Space, Tag, Tooltip, Typography} from "antd";
import {
    ViewText,
    FieldDateRange,
    HttpClient,
    getMessageApi,
    Page,
    PageUtils,
    PermUtils,
    ProTable
} from "../../../framework";

export default class LogPage extends React.Component {

    state = {
        detailRecord: null,
        fingerprintRecord: null,
        fingerprintSaving: false,
    };

    actionRef = React.createRef();
    fingerprintFormRef = React.createRef();

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
            title: (
                <Tooltip title='浏览器按设备特征生成：同一浏览器的值相同，更换设备/浏览器或清除浏览器数据后会变化'>
                    浏览器指纹
                </Tooltip>
            ),
            dataIndex: 'fingerprint',
            width: 190,
            render: (v, record) => this.renderFingerprint(v, record),
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

    canEditFingerprint = () => PermUtils.hasPermission('sys-log:write');

    renderFingerprint = (fingerprint, record) => {
        if (!fingerprint) {
            return '-';
        }
        const name = record.fingerprintName;
        const label = name || fingerprint;
        const tooltip = [
            name ? `指纹：${fingerprint}` : fingerprint,
            record.fingerprintRemark,
            this.canEditFingerprint() ? '点击编辑备注' : null,
        ].filter(Boolean).join('\n');
        const textStyle = name
            ? undefined
            : {fontFamily: 'monospace', fontSize: 12};
        const copyable = {text: fingerprint, tooltips: ['复制指纹', '已复制']};
        const tooltipTitle = <span style={{whiteSpace: 'pre-line'}}>{tooltip}</span>;

        if (this.canEditFingerprint()) {
            return (
                <Tooltip title={tooltipTitle}>
                    <Typography.Link copyable={copyable} style={textStyle}
                                     onClick={() => this.setState({fingerprintRecord: record})}>
                        {label}
                    </Typography.Link>
                </Tooltip>
            );
        }
        return (
            <Tooltip title={tooltipTitle}>
                <Typography.Text copyable={copyable} style={textStyle}>{label}</Typography.Text>
            </Tooltip>
        );
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

    saveFingerprintRemark = (values) => {
        const record = this.state.fingerprintRecord;
        if (!record) {
            return;
        }
        this.setState({fingerprintSaving: true});
        HttpClient.post('admin/sys-browser-fingerprint/save', {
            fingerprint: record.fingerprint,
            name: values.name,
            remark: values.remark,
        }).then((rs) => {
            const saved = rs.data;
            const name = saved?.name ?? null;
            const remark = saved?.remark ?? null;
            this.setState(({detailRecord}) => ({
                fingerprintRecord: null,
                fingerprintSaving: false,
                detailRecord: detailRecord && detailRecord.fingerprint === record.fingerprint
                    ? {
                        ...detailRecord,
                        fingerprintRemarkId: saved?.id ?? null,
                        fingerprintName: name,
                        fingerprintRemark: remark,
                    }
                    : detailRecord,
            }));
            this.actionRef.current?.reload();
        }).catch(() => {
            this.setState({fingerprintSaving: false});
        });
    };

    cancelFingerprintMark = (record) => {
        if (!record?.fingerprintRemarkId) {
            return;
        }
        this.setState({fingerprintSaving: true});
        HttpClient.post('admin/sys-browser-fingerprint/delete', {id: record.fingerprintRemarkId}).then(() => {
            const updated = {
                ...record,
                fingerprintRemarkId: null,
                fingerprintName: null,
                fingerprintRemark: null,
            };
            this.setState(({detailRecord}) => ({
                fingerprintRecord: null,
                fingerprintSaving: false,
                detailRecord: detailRecord && detailRecord.fingerprint === record.fingerprint ? updated : detailRecord,
            }));
            this.actionRef.current?.reload();
        }).catch(() => {
            this.setState({fingerprintSaving: false});
        });
    };

    renderFingerprintModal = () => {
        const record = this.state.fingerprintRecord;
        return (
            <Modal title='标记浏览器指纹'
                   open={!!record}
                   destroyOnHidden
                   footer={
                       <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
                           <div>
                               {record?.fingerprintRemarkId && (
                                   <Popconfirm title='取消后该指纹将恢复显示原始指纹，确定取消标记？'
                                               okText='取消标记'
                                               cancelText='返回'
                                               onConfirm={() => this.cancelFingerprintMark(record)}>
                                       <Button danger type='link' style={{paddingLeft: 0}}>取消标记</Button>
                                   </Popconfirm>
                               )}
                           </div>
                           <Space>
                               <Button onClick={() => this.setState({fingerprintRecord: null})}>取消</Button>
                               <Button type='primary'
                                       loading={this.state.fingerprintSaving}
                                       onClick={() => this.fingerprintFormRef.current?.submit()}>保存</Button>
                           </Space>
                       </div>
                   }>
                {record && (
                    <Form ref={this.fingerprintFormRef}
                          layout='vertical'
                          initialValues={{name: record.fingerprintName, remark: record.fingerprintRemark}}
                          onFinish={this.saveFingerprintRemark}>
                        <Form.Item label='指纹'>
                            <Typography.Text copyable={{tooltips: ['复制指纹', '已复制']}}
                                             style={{fontFamily: 'monospace'}}>
                                {record.fingerprint}
                            </Typography.Text>
                        </Form.Item>
                        <Form.Item label='备注名' name='name' extra='例如：张三的办公电脑-Chrome'>
                            <Input maxLength={100} allowClear placeholder='留空则显示原始指纹'/>
                        </Form.Item>
                        <Form.Item label='说明' name='remark'
                                   extra='备注名和说明都留空保存，等同于取消标记'>
                            <Input.TextArea maxLength={500} rows={3} allowClear
                                            placeholder='可补充使用者、部门、位置等信息'/>
                        </Form.Item>
                    </Form>
                )}
            </Modal>
        );
    };

    renderDetailDrawer = () => {
        const record = this.state.detailRecord;
        const hasRemark = record && (record.fingerprintName || record.fingerprintRemark);
        return (
            <Drawer title='操作日志详情'
                    size={720}
                    open={!!record}
                    onClose={() => this.setState({detailRecord: null})}>
                {record && <>
                    <Descriptions size='small' column={1} bordered>
                        <Descriptions.Item label='操作'>{record.operation || '-'}</Descriptions.Item>
                        <Descriptions.Item label='操作详情'>{record.detail || '-'}</Descriptions.Item>
                        <Descriptions.Item label='操作者'>{record.username || '-'}</Descriptions.Item>
                        <Descriptions.Item label='时间'>{record.operationTime || '-'}</Descriptions.Item>
                        <Descriptions.Item label='IP'>{record.ip || '-'}</Descriptions.Item>
                        <Descriptions.Item label='浏览器指纹'>
                            {record.fingerprint
                                ? <Typography.Text copyable={{tooltips: ['复制', '已复制']}}
                                                   style={{fontFamily: 'monospace'}}>{record.fingerprint}</Typography.Text>
                                : '-'}
                        </Descriptions.Item>
                        <Descriptions.Item label='指纹备注'>
                            <Space size='small' wrap>
                                {hasRemark
                                    ? <Space direction='vertical' size={0}>
                                        {record.fingerprintName &&
                                            <Typography.Text strong>{record.fingerprintName}</Typography.Text>}
                                        {record.fingerprintRemark &&
                                            <Typography.Text type='secondary'>{record.fingerprintRemark}</Typography.Text>}
                                    </Space>
                                    : <Typography.Text type='secondary'>未标记</Typography.Text>}
                                {record.fingerprint && this.canEditFingerprint() && (
                                    <Button type='link' size='small'
                                            onClick={() => this.setState({fingerprintRecord: record})}>
                                        {hasRemark ? '编辑' : '标记'}
                                    </Button>
                                )}
                                {hasRemark && record.fingerprintRemarkId && this.canEditFingerprint() && (
                                    <Popconfirm title='取消后该指纹将恢复显示原始指纹，确定取消标记？'
                                                okText='取消标记'
                                                cancelText='返回'
                                                onConfirm={() => this.cancelFingerprintMark(record)}>
                                        <Button type='link' size='small' danger>取消标记</Button>
                                    </Popconfirm>
                                )}
                            </Space>
                        </Descriptions.Item>
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
                actionRef={this.actionRef}
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
                        <Form.Item label='浏览器指纹' name='fingerprint'>
                            <Input allowClear placeholder='完整指纹或备注名'/>
                        </Form.Item>
                        <Form.Item label='时间' name='dateRange'>
                            <FieldDateRange/>
                        </Form.Item>
                    </>
                )}
            />
            {this.renderDetailDrawer()}
            {this.renderFingerprintModal()}
        </Page>
    }


}

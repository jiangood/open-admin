import React from 'react'
import {Button, Form, Input, Modal, Space} from 'antd'
import {CloudUploadOutlined} from "@ant-design/icons";

import {PermActions, DictUtils, FieldDateRange, FieldDictSelect, FieldUploadFile, HttpUtils, Page, ProTable, UrlUtils, ViewImage} from "../../../framework";

export default class extends React.Component {

    state = {
        formOpen: false,
        formValues: {},
    }

    tableRef = React.createRef()
    formRef = React.createRef()

    handleDelete = row => {
        HttpUtils.post('admin/sysFile/delete', {value: row.objectName}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    handleBatchDelete = ids => {
        HttpUtils.post('admin/sysFile/deleteBatch', ids).then(rs => {
            this.tableRef.current.reload()
            this.tableRef.current.clearSelection()
        })
    }

    columns = [
        {
            title: '缩略图',
            dataIndex: 'objectName',
            width: 80,
            render: (objectName, record) => {
                if (record.type !== 'image') {
                    return '-'
                }
                return <ViewImage value={objectName} size={48}/>
            },
        },
        {
            title: '原始名称',
            tooltip: '上传时候的文件名',
            dataIndex: 'originName',
            width: 200,
        },
        {
            title: '存储名称',
            dataIndex: 'objectName',
            tooltip: '文件唯一标识id',
            width: 200,
        },
        {
            title: '文件大小',
            dataIndex: 'sizeInfo',
            width: 100,
        },
        {
            title: '扩展名',
            dataIndex: 'suffix',
            width: 90,
        },
        {
            title: 'MIME',
            dataIndex: 'mimeType',
            width: 130,
        },
        {
            title: '类型',
            dataIndex: 'type',
            width: 80,
            render(v) {
                return v || '-'
            },
        },
        {
            title: '状态',
            dataIndex: 'status',
            width: 100,
            render(status) {
                return DictUtils.dictTag('fileStatus', status) || (status || '-')
            },
        },
        {
            title: '上传时间',
            dataIndex: 'createTime',
            width: 160,
        },
        {
            title: '上传者',
            dataIndex: 'createUserLabel',
            width: 100,
        },
        {
            title: '关联表',
            dataIndex: 'joinTable',
            width: 120,
            render(v) {
                return v || '-'
            },
        },
        {
            title: '关联ID',
            dataIndex: 'joinId',
            width: 160,
            render(v) {
                return v || '-'
            },
        },
        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            width: 120,
            render: (_, record) => (
                <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '预览', onClick: () => window.open(UrlUtils.contextPath('/file/' + record.objectName), '_blank')},
                        {label: '删除', perm: 'sys-file:delete', confirm: '是否确定删除文件信息', onClick: () => this.handleDelete(record)},
                    ]}
                />
            ),
        },
    ]

    renderUploadModal = () => (
        <Modal open={this.state.formOpen} title='上传文件'
               width={800}
               onCancel={() => {
                   this.setState({formOpen: false})
                   this.tableRef.current.reload()
               }}
               footer={null}
               destroyOnHidden
        >
            <Form ref={this.formRef}
                  initialValues={this.state.formValues}
            >
                <Form.Item name='文件'>
                    <FieldUploadFile accept="*/*"/>
                </Form.Item>
            </Form>
        </Modal>
    )

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                rowSelection={{}}
                toolBarRender={(params, selection) => (
                    <Space>
                        <Button type='primary' icon={<CloudUploadOutlined/>} onClick={() => this.setState({formOpen: true})}>上传文件</Button>
                        <PermActions
                            actions={[{
                                label: `批量删除${selection.selectedRowKeys.length ? `(${selection.selectedRowKeys.length})` : ''}`,
                                perm: 'sys-file:delete',
                                danger: true,
                                disabled: !selection.selectedRowKeys.length,
                                confirm: `是否确定删除选中的 ${selection.selectedRowKeys.length} 个文件?`,
                                onClick: () => this.handleBatchDelete(selection.selectedRowKeys),
                            }]}
                        />
                    </Space>
                )}
                request={(params) => {
                    return HttpUtils.get('admin/sysFile/page', params);
                }}

                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item label='文件名' name='originName'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='对象名称' name='objectName'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='状态' name='status'>
                            <FieldDictSelect typeCode='fileStatus'/>
                        </Form.Item>
                        <Form.Item label='上传时间' name='dateRange'>
                            <FieldDateRange/>
                        </Form.Item>
                    </>
                )}
            />
            {this.renderUploadModal()}
        </Page>
    }
}

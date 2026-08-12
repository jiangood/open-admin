import React from 'react'
import {Button, Empty, Form, Input, Modal, Pagination, Tabs} from 'antd'
import {CloudUploadOutlined} from "@ant-design/icons";

import {PermActions, DictUtils, FieldDateRange, FieldDictSelect, FieldUploadFile, HttpUtils, Page, ProTable, UrlUtils} from "../../../framework";

export default class extends React.Component {

    state = {
        formOpen: false,
        formValues: {},
        gridPage: 1,
        gridList: [],
        gridTotal: 0,
        gridLoading: false,
        previewUrl: undefined,
    }

    tableRef = React.createRef()
    formRef = React.createRef()

    handleDelete = row => {
        HttpUtils.post('admin/sysFile/delete', {value: row.objectName}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    loadGrid = page => {
        const p = page || this.state.gridPage
        this.setState({gridLoading: true})
        HttpUtils.get('admin/sysFile/page', {page: p, size: 50, type: 'image'}).then(rs => {
            this.setState({gridList: rs.content || [], gridTotal: Number(rs.totalElements || 0), gridPage: p})
        }).finally(() => {
            this.setState({gridLoading: false})
        })
    }

    columns = [
        {
            title: '原始名称',
            tooltip: '上传时候的文件名',
            dataIndex: 'originName',
            width: 200,
        },
        {
            title: '状态',
            dataIndex: 'status',
            width: 120,
            render(status) {
                return DictUtils.dictTag('fileStatus', status) || (status || '-')
            },
        },
        {
            title: '存储名称',
            dataIndex: 'objectName',
            tooltip: '文件唯一标识id'
        },

        {
            title: '文件大小',
            dataIndex: 'sizeInfo',
        },

        {
            title: 'mime',
            dataIndex: 'mimeType',
        },
        {
            title: '扩展名',
            dataIndex: 'suffix',
        },

        {
            title: '上传时间',
            dataIndex: 'createTime',
        },
        {
            title: '上传者',
            dataIndex: 'createUserLabel',
        },
        {
            title: '预览',
            dataIndex: 'objectName',
            render(objectName) {
                return <a href={UrlUtils.contextPath('/file/' + objectName)} target='_blank'>预览</a>
            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <PermActions
                    more
                    size="small"
                    actions={[
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

    renderTable = () => (
        <>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <Button type='primary' icon={<CloudUploadOutlined/>} onClick={() => this.setState({formOpen: true})}>上传文件</Button>
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
        </>
    )

    renderImageGrid = () => {
        const {gridList, gridTotal, gridPage, gridLoading, previewUrl} = this.state
        return (
            <div style={{minHeight: 200, position: 'relative'}}>
                {gridList.length === 0 && !gridLoading && <Empty style={{marginTop: 80}} description="暂无图片"/>}
                <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: 8}}>
                    {gridList.map(item => (
                        <div key={item.objectName}
                             style={{borderRadius: 4, overflow: 'hidden', cursor: 'pointer', border: '1px solid #eee'}}
                             onClick={() => this.setState({previewUrl: UrlUtils.contextPath('/file/' + item.objectName)})}>
                            <img
                                src={UrlUtils.contextPath('/file/' + item.objectName + '?thumb=1')}
                                style={{width: '100%', height: 120, objectFit: 'cover', display: 'block'}}
                                alt={item.originName || item.objectName}
                            />
                        </div>
                    ))}
                </div>
                {gridTotal > 0 && (
                    <div style={{display: 'flex', justifyContent: 'flex-end', marginTop: 16}}>
                        <Pagination
                            current={gridPage}
                            pageSize={50}
                            total={gridTotal}
                            showTotal={(total) => `共 ${total} 张`}
                            onChange={(page) => this.loadGrid(page)}
                        />
                    </div>
                )}

                <Modal open={!!previewUrl} title="图片预览" width="70vw" footer={null}
                       onCancel={() => this.setState({previewUrl: undefined})}>
                    {previewUrl && <img src={previewUrl} style={{maxWidth: '100%'}} alt="预览"/>}
                </Modal>
            </div>
        )
    }

    render() {
        return <Page title="文件管理" description="管理系统上传文件">
            <Tabs
                defaultActiveKey="table"
                onChange={(key) => {
                    if (key === 'image' && this.state.gridList.length === 0) {
                        this.loadGrid(1)
                    }
                }}
                items={[
                    {key: 'table', label: '全部', children: this.renderTable()},
                    {key: 'image', label: '图片', children: this.renderImageGrid()},
                ]}
            />
        </Page>
    }
}

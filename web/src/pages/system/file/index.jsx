import React from 'react'
import {Button, Form, Input, Modal} from 'antd'
import {CloudUploadOutlined} from "@ant-design/icons";

import {PermActions, DictUtils, FieldDateRange, FieldDictSelect, FieldUploadFile, HttpUtils, Page, ProTable, UrlUtils} from "../../../framework";

export default class extends React.Component {

    state = {
        formOpen: false,
        formValues: {}
    }

    tableRef = React.createRef()
    formRef = React.createRef()

    handleDelete = row => {
        HttpUtils.post('admin/sysFile/delete', {value: row.objectName}).then(rs => {
            this.tableRef.current.reload()
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

    render() {
        return <Page title="文件管理" description="管理系统上传文件">
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <PermActions>
                        <Button type='primary' perm='sys-file:upload' icon={<CloudUploadOutlined/>} onClick={() => this.setState({formOpen: true})}>上传文件</Button>
                    </PermActions>
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
                        <Form.Item label='类型' name='type'>
                            <FieldDictSelect typeCode='materialType'/>
                        </Form.Item>
                        <Form.Item label='上传时间' name='dateRange'>
                            <FieldDateRange/>
                        </Form.Item>
                    </>
                )}
            />

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

        </Page>
    }
}




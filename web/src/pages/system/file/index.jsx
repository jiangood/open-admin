import React from 'react'
import {Button, Card, Empty, Form, Input, Modal, Space, Splitter, Tree, Typography} from 'antd'
import {CloudUploadOutlined, FolderOpenOutlined, FolderOutlined, ReloadOutlined} from "@ant-design/icons";

import {PermActions, DictUtils, FieldDateRange, FieldDictSelect, FieldUploadFile, HttpClient, Page, ProTable, UrlUtils, ViewImage} from "../../../framework";

const ALL_FOLDER_KEY = '__all__'

export default class FilePage extends React.Component {

    state = {
        formOpen: false,
        formValues: {},
        folderTree: [],
        treeLoading: true,
        currentFolder: null,
    }

    tableRef = React.createRef()
    formRef = React.createRef()

    componentDidMount() {
        this.loadFolderTree()
    }

    loadFolderTree = () => {
        this.setState({treeLoading: true})
        HttpClient.get('admin/sys-file/folder-tree', null, {toastError: false}).then(rs => {
            const folderTree = rs.data || []
            const {currentFolder} = this.state
            const folderExists = !currentFolder || !!this.findFolder(folderTree, currentFolder)
            this.setState({
                folderTree,
                treeLoading: false,
                currentFolder: folderExists ? currentFolder : null,
            }, () => {
                // 当前文件夹下的文件已全部删除时，回到「全部文件」
                if (currentFolder && !folderExists) {
                    this.tableRef.current?.reload()
                }
            })
        }).catch(() => {
            this.setState({treeLoading: false})
        })
    }

    findFolder = (nodes, key) => {
        for (const node of nodes || []) {
            if (node.key === key) return node
            const found = this.findFolder(node.children, key)
            if (found) return found
        }
        return null
    }

    renderFolderTitle = (title, count) => (
        <span>
            {title}
            <Typography.Text type='secondary' style={{marginLeft: 8, fontSize: 12}}>{count}</Typography.Text>
        </span>
    )

    buildTreeData = (nodes) => (nodes || []).map(node => ({
        key: node.key,
        title: this.renderFolderTitle(node.title, node.count),
        children: node.children && node.children.length > 0 ? this.buildTreeData(node.children) : undefined,
    }))

    onFolderSelect = selectedKeys => {
        const key = selectedKeys[0]
        const currentFolder = !key || key === ALL_FOLDER_KEY ? null : key
        this.setState({currentFolder}, () => this.tableRef.current?.reload())
    }

    handleDelete = row => {
        HttpClient.post('admin/sys-file/delete', {value: row.objectName}, null).then(() => {
            this.tableRef.current.reload()
            this.loadFolderTree()
        })
    }

    handleBatchDelete = ids => {
        HttpClient.post('admin/sys-file/delete-batch', ids, null).then(() => {
            this.tableRef.current.reload()
            this.tableRef.current.clearSelection()
            this.loadFolderTree()
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
                   this.loadFolderTree()
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

    renderFolderTree = () => {
        const {folderTree, treeLoading, currentFolder} = this.state
        const total = folderTree.reduce((sum, node) => sum + (node.count || 0), 0)
        const treeData = folderTree.length > 0 ? [{
            key: ALL_FOLDER_KEY,
            title: this.renderFolderTitle('全部文件', total),
            children: this.buildTreeData(folderTree),
        }] : []

        return <Card size='small'
                     loading={treeLoading}
                     title={<div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                         <Typography.Text strong>文件夹</Typography.Text>
                         <Button type='text' size='small' icon={<ReloadOutlined/>} title='刷新文件夹' onClick={this.loadFolderTree}/>
                     </div>}
        >
            {treeData.length > 0 ? <Tree
                    treeData={treeData}
                    defaultExpandAll
                    showLine
                    showIcon
                    blockNode
                    icon={props => props.eventKey === ALL_FOLDER_KEY ? <FolderOpenOutlined/> : <FolderOutlined/>}
                    selectedKeys={currentFolder ? [currentFolder] : [ALL_FOLDER_KEY]}
                    onSelect={this.onFolderSelect}
                />
                : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description='暂无文件夹'/>}
        </Card>
    }

    render() {
        return <Page>
            <Splitter>
                <Splitter.Panel defaultSize={280} style={{paddingRight: 8}}>
                    {this.renderFolderTree()}
                </Splitter.Panel>
                <Splitter.Panel style={{paddingLeft: 8}}>
                    <ProTable
                        actionRef={this.tableRef}
                        rowSelection={{}}
                        toolBarRightRender={(params, selection) => (
                            <Space>
                                <Button type='primary' icon={<CloudUploadOutlined/>} onClick={() => this.setState({formOpen: true})}>上传文件</Button>
                                <PermActions
                                    actions={[{
                                        label: '批量删除' + (selection.selectedRowKeys.length ? `(${selection.selectedRowKeys.length})` : ''),
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
                            if (this.state.currentFolder) {
                                params.folder = this.state.currentFolder
                            }
                            return HttpClient.get('admin/sys-file/page', params);
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
                </Splitter.Panel>
            </Splitter>
            {this.renderUploadModal()}
        </Page>
    }
}

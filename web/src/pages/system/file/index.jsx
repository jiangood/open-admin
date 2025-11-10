import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {
    ButtonList,
    FieldDateRange,
    FieldDictSelect,
    FieldUploadFile,
    HttpUtil,
    ProTable,
    SysUtil
} from "@tmgg/tmgg-base";
import {CloudUploadOutlined} from "@ant-design/icons";


export default class extends React.Component {

    state = {
        formOpen: false,
        formValues: {}
    }

    tableRef = React.createRef()
    formRef = React.createRef()


    columns = [
        {
            title: '原始名称',
            tooltip: '上传时候的文件名',
            dataIndex: 'originName',
            width:200,
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
            dataIndex: 'id',
            render(id,record){
                const nodes = [  <a href={SysUtil.wrapServerUrl( 'sysFile/preview/' + record.id) } target='_blank'>预览</a>]


                if(record.imageUrls?.length > 0){
                   for(let item of record.imageUrls){
                       let url = item.url;
                       let label = item.label;
                       nodes.push(<a href={SysUtil.wrapServerUrl( url) } target='_blank' style={{marginLeft:8}} >{label}</a>)
                   }
                }

                return nodes;

            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Popconfirm perm='sysFile:delete' title='是否确定删除文件信息'
                                onConfirm={() => this.handleDelete(record)}>
                        <a>删除</a>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]


    handleDelete = row => {
        HttpUtil.get('sysFile/delete', row).then(rs => {
            this.tableRef.current.reload()
        })
    }



    render() {
        return <>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => {
                    return <Button type='primary' icon={<CloudUploadOutlined/>}
                                   onClick={() => this.setState({formOpen: true})}>
                        上传文件
                    </Button>
                }}
                request={(params) => {
                    return HttpUtil.pageData('sysFile/page', params);
                }}

                columns={this.columns}

            >
                <Form.Item label='文件名' name='originName'>
                    <Input/>
                </Form.Item>
                <Form.Item label='对象名称' name='objectName'>
                    <Input/>
                </Form.Item>
                <Form.Item label='类型' name='type'>
                    <FieldDictSelect typeCode='materialType'/>
                </Form.Item>

                <Form.Item label='上传时间' name='dateRange'>
                    <FieldDateRange/>
                </Form.Item>
            </ProTable>

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

        </>
    }
}




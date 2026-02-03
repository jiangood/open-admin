import {DeleteOutlined, EditOutlined, PlusOutlined, SyncOutlined} from '@ant-design/icons';
import {Button, Card, Checkbox, Empty, Form, Input, Popconfirm, Space, Splitter, Switch, Tree} from 'antd';
import React from 'react';
import {
    FieldBoolean,
    FieldDictSelect,
    FieldRemoteTreeSelect,
    FieldUserSelect,
    Gap,
    HttpUtils,
    NamedIcon,
    Page
} from "../../../framework";


export default class extends React.Component {

    state = {
        formLoading: false,
        formValues: undefined,
        submitLoading: false,
        formEditing: false,


        params: {
            onlyShowEnabled: true,
            onlyShowUnit: false,
            searchText: null
        },

        treeData: [],
        treeLoading: false,
        draggable: false,
    }
    actionRef = React.createRef();
    treeRef = React.createRef();

    componentDidMount() {
        this.loadTree()
    }

    loadTree = () => {
        this.setState({treeLoading: true})

        const {params} = this.state
        HttpUtils.get('admin/sysOrg/tree', params).then(rs => {
            let treeData = rs;
            this.setState({treeData})
        }).finally(() => {
            this.setState({treeLoading: false});
        })
    }

    handleDelete = record => {
        HttpUtils.post('admin/sysOrg/delete', {id: record.id})
            .then(rs => {
                this.setState({formValues: null})
                this.loadTree()
            })
    }


    onSelect = (selectedKeys) => {
        if (selectedKeys.length === 0) {
            this.setState({formValues: null})
            return
        }

        const key = selectedKeys[0]
        this.setState({formLoading: true, formEditing: false})
        HttpUtils.get("admin/sysOrg/detail", {id: key}).then(rs => {
            this.setState({formValues: rs})
        }).finally(() => {
            this.setState({formLoading: false})
        })
    }

    onFinish = (values) => {
        this.setState({submitLoading: true, formEditing: false})
        HttpUtils.post('admin/sysOrg/save', values).then(rs => {
            this.loadTree()
        }).finally(() => {
            this.setState({submitLoading: false})
        })
    }


    onDraggableChange = e => {
        this.setState({draggable: e})
    };


    render() {
        let {formValues} = this.state;
        let disabled = formValues == null;
        let params = this.state.params;
        return <Page padding>
            <Card>
                <Space>

                    <Input.Search placeholder='搜索' value={params.searchText} onChange={e => {
                        params.searchText = e.target.value
                        this.setState({params}, this.loadTree)
                    }}/>
                    <Checkbox checked={params.onlyShowEnabled}
                              onChange={e => {
                                  params.onlyShowEnabled = e.target.checked;
                                  this.setState({params}, this.loadTree);
                              }}>仅显示启用</Checkbox>

                    <Checkbox checked={params.onlyShowUnit}
                              onChange={e => {
                                  params.onlyShowUnit = e.target.checked;
                                  this.setState({params}, this.loadTree);
                              }}>仅显示单位</Checkbox>


                </Space>
            </Card>
            <Gap/>
            <Splitter>
                <Splitter.Panel defaultSize={500}>
                    <Card loading={this.state.treeLoading}
                          title='组织机构'
                          extra={<Space>

                              <div>
                                  拖拽排序&nbsp;<Switch
                                  value={this.state.draggable}
                                  onChange={this.onDraggableChange}/>
                              </div>
                              <Button size='small' shape={"round"} icon={<SyncOutlined/>}
                                      onClick={this.loadTree}></Button>
                          </Space>}>


                        <Tree ref={this.treeRef}
                              treeData={this.state.treeData}
                              onSelect={this.onSelect}
                              showIcon
                              blockNode
                              icon={item => {
                                  return <NamedIcon name={item.data.iconName}/>;
                              }}
                              draggable={this.state.draggable}
                              onDrop={this.onDrop}
                              showLine
                              defaultExpandAll
                        />
                        {this.state.treeData.length === 0 && <Empty/>}
                    </Card>
                </Splitter.Panel>

                <Splitter>
                    <Card
                        loading={this.state.formLoading}
                        extra={<Space>
                            <Button type='primary' onClick={() => {
                                this.setState({
                                    formLoading: true,
                                    formEditing: true,
                                    formValues: {
                                        pid: formValues?.id,
                                    }
                                }, () => {
                                    this.setState({formLoading: false})
                                })
                            }}>
                                <PlusOutlined/> 新增
                            </Button>
                            <Button disabled={disabled} onClick={() => {
                                this.setState({
                                    formEditing: true
                                })
                            }}>
                                <EditOutlined/> 编辑
                            </Button>

                            <Popconfirm title={'是否确定删除组织机构'} disabled={disabled}
                                        onConfirm={() => this.handleDelete(formValues)}>
                                <Button icon={<DeleteOutlined/>} disabled={disabled}>删除</Button>
                            </Popconfirm>


                        </Space>}
                    >

                        {formValues == null ? <Empty description='未选择机构'/> : <Form
                            disabled={!this.state.formEditing}
                            labelCol={{flex: '150px'}}
                            wrapperCol={{flex: '400px'}}
                            initialValues={formValues}
                            onFinish={this.onFinish}
                        >
                            <Form.Item noStyle name='id'>
                            </Form.Item>
                            <Form.Item label='父节点' name='pid'>
                                <FieldRemoteTreeSelect url='admin/sysOrg/tree'/>
                            </Form.Item>
                            <Form.Item label='名称' name='name' rules={[{required: true}]}>
                                <Input/>
                            </Form.Item>


                            <Form.Item label='类型' name='type' rules={[{required: true}]}>
                                <FieldDictSelect typeCode='orgType'/>
                            </Form.Item>

                            <Form.Item label='部门领导' name={['leader', 'id']}>
                                <FieldUserSelect/>
                            </Form.Item>
                            <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                                <FieldBoolean/>
                            </Form.Item>
                            <Form.Item label='扩展字段1' name='extra1'>
                                <Input/>
                            </Form.Item>
                            <Form.Item label='扩展字段2' name='extra2'>
                                <Input/>
                            </Form.Item>
                            <Form.Item label='扩展字段3' name='extra3'>
                                <Input/>
                            </Form.Item>


                            <Form.Item label=' ' colon={false}>
                                <Button type="primary" htmlType='submit'
                                        loading={this.state.submitLoading}>保存</Button>
                            </Form.Item>

                        </Form>
                        }


                    </Card>
                </Splitter>

            </Splitter>


        </Page>
    }

    onDrop = (e) => {
        const {dragNode, dropToGap, node} = e;
        const dropKey = node.key;
        const dragKey = dragNode.key;

        const dropPos = e.node.pos.split('-');
        const dropPosition = e.dropPosition - Number(dropPos[dropPos.length - 1]); // the drop position relative to the drop node, inside 0, top -1, bottom 1


        console.log(dragNode.title, '->', node.title, 'dropToGap:', dropToGap, "dropPosition:", dropPosition)
        HttpUtils.post('admin/sysOrg/sort', {dropPosition, dropToGap, dropKey, dragKey}).then(this.loadTree)
    };
}




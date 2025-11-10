import {DeleteOutlined, EditOutlined, PlusOutlined, SyncOutlined} from '@ant-design/icons';
import {Button, Card, Empty, Form, Input, Popconfirm, Select, Space, Splitter, Switch, Tree} from 'antd';
import React from 'react';
import {
    FieldDictRadio,
    FieldRadioBoolean,
    FieldRemoteTreeSelect, FieldSelect, FieldUserSelect,
    Gap,
    HttpUtil,
    NamedIcon,
    Page
} from "@tmgg/tmgg-base";
import {TreeUtil} from "@tmgg/tmgg-commons-lang";

const baseTitle = "组织机构";
const baseApi = 'sysOrg/';

const deleteTitle = '删除' + baseTitle


const treeApi = baseApi + 'allTree'


export default class extends React.Component {

    state = {
        formLoading: false,
        formValues: undefined,
        submitLoading: false,
        formEditing: false,


        params: {
            showDisabled: true,
            showDept: true,
            searchText: null
        },

        treeData: [],
        treeLoading: false,
        draggable: false,

        expandedKeys: [],


        enableAllLoading: false,


    }
    actionRef = React.createRef();
    treeRef = React.createRef();

    componentDidMount() {
        this.loadTree(true)
    }

    loadTree = (showLoading) => {
        if (showLoading) {
            this.setState({treeLoading: true})
        }

        const {params} = this.state
        HttpUtil.post('sysOrg/pageTree', params).then(rs => {
            let treeData = rs;
            this.setState({treeData})
        }).finally(() => {
            this.setState({treeLoading: false});
        })
    }

    handleDelete = row => {
        HttpUtil.create()
            .enableShowMessage()
            .enableShowLoading()
            .post('sysOrg/delete', row)
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
        HttpUtil.get(baseApi + "detail", {id: key}).then(rs => {
            this.setState({formValues: rs})
        }).finally(() => {
            this.setState({formLoading: false})
        })
    }

    onFinish = (values) => {
        this.setState({submitLoading: true, formEditing: false})
        HttpUtil.post(baseApi + 'save', values).then(rs => {
            this.loadTree()
        }).finally(() => {
            this.setState({submitLoading: false})
        })
    }
    handleEnableAll = (id) => {
        this.setState({enableAllLoading: true})
        HttpUtil.get(baseApi + 'enableAll', {id}).then(rs => {
            this.loadTree()
        }).finally(() => {
            this.setState({enableAllLoading: false})
        })
    }

    handleDisableAll = (id) => {
        this.setState({enableAllLoading: true})
        HttpUtil.get(baseApi + 'disableAll', {id}).then(rs => {
            this.loadTree()
        }).finally(() => {
            this.setState({enableAllLoading: false})
        })
    }

    onDraggableChange = e => {
        this.setState({draggable: e})
    };
    onExpandSelect = v => {
        const keys = TreeUtil.findKeysByLevel(this.state.treeData, v)
        this.setState({expandedKeys: keys})
    }

    render() {
        let {formValues} = this.state;
        let disabled = formValues == null;
        let params = this.state.params;
        return <Page>
            <Splitter>
                <Splitter.Panel defaultSize={500}>
                    <Card loading={this.state.treeLoading}
                          title='组织机构'
                          extra={<Space>
                              <div>
                                  展开 <Select style={{width: 80}}
                                               size='small'
                                               options={[
                                                   {label: '所有', value: -1},
                                                   {label: '一级', value: 1},
                                                   {label: '二级', value: 2},
                                                   {label: '三级', value: 3}]}
                                               onChange={this.onExpandSelect}
                              />
                              </div>
                              <div>
                                  拖拽排序&nbsp;<Switch
                                  value={this.state.draggable}
                                  onChange={this.onDraggableChange}/>
                              </div>
                              <Button size='small' shape={"round"} icon={<SyncOutlined/>}
                                      onClick={this.loadTree}></Button>
                          </Space>}>
                        <Space>

                            <Input.Search placeholder='搜索' value={params.searchText} onChange={e => {
                                params.searchText = e.target.value
                                this.setState({params}, this.loadTree)
                            }}/>
                            <div>
                                显示禁用 <Switch
                                value={params.showDisabled}
                                onChange={e => {
                                    params.showDisabled = e;
                                    this.setState({params}, this.loadTree);
                                }}
                            />
                            </div>
                            <div>
                                显示部门 <Switch
                                value={params.showDept}
                                onChange={e => {
                                    params.showDept = e;
                                    this.setState({params}, this.loadTree);
                                }}
                            />
                            </div>
                        </Space>

                        <Gap/>


                        <Tree ref={this.treeRef}
                              treeData={this.state.treeData}
                              onSelect={this.onSelect}
                              showIcon
                              blockNode
                              icon={item => <NamedIcon name={item.data.iconName}/>}
                              draggable={this.state.draggable}
                              onDrop={this.onDrop}
                              showLine
                              expandedKeys={this.state.expandedKeys}
                              onExpand={(expandedKeys) => {
                                  this.setState({expandedKeys})
                              }}
                              autoExpandParent
                        >
                        </Tree>
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

                            <Popconfirm title={'是否确定' + deleteTitle} disabled={disabled}
                                        onConfirm={() => this.handleDelete(formValues)}>
                                <Button icon={<DeleteOutlined/>} disabled={disabled}>删除</Button>
                            </Popconfirm>

                            <Popconfirm
                                title='启用本级及子节点'
                                disabled={disabled}
                                onConfirm={() => this.handleEnableAll(formValues?.id)}>
                                <Button disabled={disabled}>启用本级及子节点</Button>
                            </Popconfirm>

                            <Popconfirm title='禁用本级及子节点' disabled={disabled}
                                        onConfirm={() => this.handleDisableAll(formValues?.id)}>
                                <Button disabled={disabled}>禁用本级及子节点</Button>
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
                                <FieldRemoteTreeSelect url={treeApi}/>
                            </Form.Item>
                            <Form.Item label='名称' name='name' rules={[{required: true}]}>
                                <Input />
                            </Form.Item>


                            <Form.Item label='类型' name='type' rules={[{required: true}]}>
                                <FieldDictRadio typeCode='orgType'/>
                            </Form.Item>

                            <Form.Item label='部门领导' name={['leader','id']} >
                                <FieldUserSelect />
                            </Form.Item>
                            <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                                <FieldRadioBoolean/>
                            </Form.Item>
                            <Form.Item label='扩展字段1' name='extra1'>
                                <Input />
                            </Form.Item>
                            <Form.Item label='扩展字段2' name='extra2'>
                                <Input />
                            </Form.Item>
                            <Form.Item label='扩展字段3' name='extra3'>
                                <Input />
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

    onDrop = ({dragNode, dropPosition, dropToGap, node}) => {
        const dropKey = node.key;
        const dragKey = dragNode.key;
        console.log(dragNode.title, '->', node.title, 'dropToGap', dropToGap, dropPosition)
        HttpUtil.post('/sysOrg/sort', {dropPosition, dropToGap, dropKey, dragKey}).then(this.loadTree)
    };
}




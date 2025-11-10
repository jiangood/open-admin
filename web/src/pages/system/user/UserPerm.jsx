import {Form, Modal, Spin, Tree} from 'antd';
import React from 'react';
import {FieldDictSelect,  FieldSelect, HttpUtil} from "@tmgg/tmgg-base";


export default class UserPerm extends React.Component {


    state = {
        visible: false,

        confirmLoading: false,

        formValues: {
            dataPermType: null
        },
    }
    show(item) {
        this.setState({visible: true})

        HttpUtil.get('/sysUser/getPermInfo', {id: item.id}).then(rs => {
            this.setState({formValues: rs})
            this.formRef.current.setFieldsValue(rs)
        })
    }

    handleSave = (values) => {
        values.grantOrgIdList = this.state.checked

        this.setState({
            confirmLoading: true
        })


        HttpUtil.post('sysUser/grantPerm', values).then(rs => {
            this.setState({
                visible: false,
                confirmLoading: false
            })
            this.props.onOk()
        }).finally(()=>{
            this.setState({
                confirmLoading: false
            })

        })
    }



    formRef = React.createRef()


    render() {
        let {visible, treeData, confirmLoading, checked} = this.state

        return <Modal
            title="授权"
            destroyOnHidden
            width={600}
            open={visible}
            confirmLoading={confirmLoading}
            onCancel={() => this.setState({visible: false})}
            onOk={() => this.formRef.current.submit()}
        >

            <Form ref={this.formRef}
                  onFinish={this.handleSave}
                  onValuesChange={(change, values) => {
                      this.setState({formValues: values})
                  }}
                  labelCol={{flex: '100px'}}
            >
                <Form.Item name='id' noStyle></Form.Item>
                <Form.Item label='角色' name='roleIds' rules={[{required: true}]}>
                    <FieldSelect url='sysRole/options' multiple/>
                </Form.Item>
                <Form.Item label='数据权限' name='dataPermType' rules={[{required: true}]}>
                    <FieldDictSelect typeCode='dataPermType' />
                </Form.Item>


                {this.state.formValues.dataPermType === 'CUSTOM' && <>
                    <Form.Item label='组织机构' name='orgIds'>
                        <FieldTree/>
                    </Form.Item>
                </>}


            </Form>


        </Modal>
    }


}

class FieldTree extends React.Component {

    state = {
        treeLoading: true,
        treeData: [],
    }

    componentDidMount() {
        HttpUtil.get('/sysOrg/unitTree').then(treeData => {
            this.setState({treeData, treeLoading: false})
        })
    }


    render() {
        if (this.state.treeLoading) {
            return <Spin />
        }
        return <Tree
            multiple
            checkable
            onCheck={e => this.props.onChange(e.checked)}
            checkedKeys={this.props.value}
            treeData={this.state.treeData}
            defaultExpandAll
            checkStrictly
        >
        </Tree>
    }
}





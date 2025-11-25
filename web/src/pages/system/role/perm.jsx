import React from "react";
import {Alert, Button, Card, Checkbox, Table, Typography} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import {ArrUtils, HttpUtils, PageUtils} from "../../../framework";

export default class extends React.Component {


    state = {
        loading: false,
        roleInfo: {},

        dataSource: [],
        rowSelectedKeys: {},
    }

    columns = [
        {
            title: '菜单',
            dataIndex: 'name',
        },

        {
            title: '权限',
            dataIndex: 'perms',
            render: (perms, record) => {
                if (perms == null) {
                    if (record.path) {
                        return <Alert type='error' message='错误：无按钮权限，请在application-data-**.yml中添加'/>
                    }
                    // 文件夹
                    return
                }
                const options = perms.map(perm => {
                    return {
                        label: perm.name,
                        value: perm.perm
                    }
                })


                let rowSelectedKey = this.state.rowSelectedKeys[record.id];
                return <Checkbox.Group options={options}
                                       value={rowSelectedKey}
                                       onChange={(ks) => {
                                           const rowSelectedKeys = this.state.rowSelectedKeys;
                                           rowSelectedKeys[record.id] = ks;
                                           this.setState({rowSelectedKeys})
                                       }}/>
            }
        }
    ]

    componentDidMount() {
        this.roleId = PageUtils.currentParams().id;
        this.loadData();
    }


    loadData() {
        this.setState({loading: true})
        Promise.all([
            HttpUtils.get('admin/sysRole/get', {id: this.roleId}).then(rs => {
                this.setState({roleInfo: rs})
            }),
            HttpUtils.get('admin/sysRole/permTreeTable', {id: this.roleId}).then(rs => {
                this.setState({dataSource: rs})
            }),
            HttpUtils.get('admin/sysRole/ownPerms', {id: this.roleId}).then(rs => {
                this.setState({rowSelectedKeys: rs})
            })
        ]).then(rs => {
            this.setState({loading: false})
        })
    }

    savePerms = () => {
        const {rowSelectedKeys} = this.state;
        const perms = [];
        const menus = []
        for (let menuId in rowSelectedKeys) {
            const ks = rowSelectedKeys[menuId];
            if (ks == null || ks.length === 0) {
                continue;
            }
            menus.push(menuId)
            ArrUtils.addAll(perms, ks)
        }
        HttpUtils.post('admin/sysRole/savePerms', {id: this.roleId, perms, menus}).then(rs => {
            //  Page.open(PageUtils.currentPathname(), PageUtils.currentLabel())
        })
    };


    render() {
        return <>


            <Card title='角色权限设置' loading={this.state.loading}
                  variant={"borderless"}
                  extra={<Button type='primary' icon={<SaveOutlined/>} onClick={this.savePerms}>保存权限</Button>}>
                <Typography.Text>角色名称：{this.state.roleInfo.name}， 编码：{this.state.roleInfo.code} </Typography.Text>
                <Table dataSource={this.state.dataSource}
                       columns={this.columns}
                       size='small' bordered pagination={false} rowKey='id'
                       expandable={{defaultExpandAllRows: true}}
                ></Table>
            </Card>
        </>
    }
}

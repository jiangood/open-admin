import React from "react";
import {Alert, Button, Card, Checkbox, Flex, Table, Typography} from "antd";
import {HttpUtil, Page, PageUtil} from "@tmgg/tmgg-base";
import {SaveOutlined} from "@ant-design/icons";
import {ArrUtil} from "@tmgg/tmgg-commons-lang";

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
        this.roleId = PageUtil.currentParams().id;
        this.loadData();
    }


    loadData() {
        this.setState({loading: true})
        Promise.all([
            HttpUtil.get('sysRole/get', {id: this.roleId}).then(rs => {
                this.setState({roleInfo: rs})
            }),
            HttpUtil.get('sysRole/permTreeTable', {id: this.roleId}).then(rs => {
                this.setState({dataSource: rs})
            }),
            HttpUtil.get('sysRole/ownPerms', {id: this.roleId}).then(rs => {
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
            ArrUtil.addAll(perms, ks)
        }
        HttpUtil.post('sysRole/savePerms', {id: this.roleId, perms, menus}).then(rs => {
            //  Page.open(PageUtil.currentPathname(), PageUtil.currentLabel())
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
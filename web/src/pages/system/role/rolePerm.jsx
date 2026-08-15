import React from "react";
import {Button, Card, Checkbox, Table, Typography} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import {HttpClient, Page} from "../../../framework";
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
            dataIndex: 'permCodes',
            render: (permCodes, record) => {
                if (permCodes == null) {
                    return
                }
                const {permNames} = record
                const options = [];
                for (let i = 0; i < permCodes.length; i++) {
                    const label = permNames[i];
                    const value = permCodes[i];
                    options.push({label, value});
                }


                const rowSelectedKey = this.state.rowSelectedKeys[record.id];
                return <Checkbox.Group options={options}
                                       value={rowSelectedKey}
                                       onChange={(ks) => {
                                           this.setState(prevState => ({
                                               rowSelectedKeys: {...prevState.rowSelectedKeys, [record.id]: ks}
                                           }))
                                       }}/>
            }
        }
    ]

    componentDidMount() {
        this.roleId = this.props.location?.query?.id;
        this.loadData();
    }


    loadData() {
        this.setState({loading: true})
        const requestCount = 3;
        let finished = 0;
        const onFinish = () => {
            if (++finished === requestCount) {
                this.setState({loading: false})
            }
        };
        HttpClient.get('admin/sysRole/get', {id: this.roleId}, rs => {
            this.setState({roleInfo: rs})
            onFinish()
        });
        HttpClient.get('admin/sysRole/perm-tree-table', {id: this.roleId}, rs => {
            this.setState({dataSource: rs})
            onFinish()
        });
        HttpClient.get('admin/sysRole/own-perms', {id: this.roleId}, rs => {
            this.setState({rowSelectedKeys: rs})
            onFinish()
        });
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
            perms.push(...ks)
        }
        HttpClient.post('admin/sysRole/save-perms', {id: this.roleId, perms, menus})
    };


    render() {
        return <Page title="角色权限设置" description="设置角色的菜单和权限">
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
        </Page>
    }
}

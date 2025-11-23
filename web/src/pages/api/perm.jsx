import {message, Switch} from 'antd'
import React from 'react'
import {ArrUtil, HttpUtil, Page, PageUtil, ProTable} from "../../framework";


export default class extends React.Component {


    constructor(props) {
        super(props);
        this.accountId = PageUtil.currentParams().accountId
    }

    state = {
        perms: []
    }

    async componentDidMount() {
        const rs = await HttpUtil.get('admin/apiAccount/get', {id: this.accountId})
        this.setState({perms: rs.perms})
    }

    onChange = async (action, checked) => {
        const hide = message.loading('保存中...', 0)
        await HttpUtil.post('admin/apiAccount/grant', {accountId: this.accountId, action, checked});

        const perms = this.state.perms
        if (checked) {
            ArrUtil.add(perms, action)
        } else {
            ArrUtil.remove(perms, action)
        }
        this.setState({perms})
        hide();
    }


    render() {
        return <Page>


            <ProTable
                rowKey='action'
                columns={[
                    {dataIndex: 'name', title: '名称'},
                    {dataIndex: 'action', title: '动作'},
                    {dataIndex: 'desc', title: '描述'},
                    {
                        dataIndex: 'option', title: '操作',
                        render: (_, record) => {
                            let action = record.action;
                            return <Switch checked={this.state.perms.includes(action)}
                                           onChange={(checked) => {
                                               this.onChange(action, checked)

                                           }}> </Switch>
                        }
                    }
                ]}
                request={(params,) => HttpUtil.get('admin/api/resource/page', params)}
            />

        </Page>


    }
}



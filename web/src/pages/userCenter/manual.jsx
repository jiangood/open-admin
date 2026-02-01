import React from 'react'
import {HttpUtils, Page, ProTable} from "../../framework";


export default class extends React.Component {


    tableRef = React.createRef()

    columns = [

        {
            title: '名称',
            dataIndex: 'name',


        },

        {
            title: '版本',
            dataIndex: 'version',
            render(version) {
                return 'v' + version;
            }
        },

        {
            title: '文件',
            dataIndex: 'fileId',
            render(id) {
                const url = ('admin/sysFile/preview/' + id);
                return <a href={url} target='_blank'>查看文件</a>
            }

        },

        {
            title: '更新时间',
            dataIndex: 'updateTime',
        },
    ]


    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                request={(params) => HttpUtils.get('admin/sysManual/pageForUser', params)}
                columns={this.columns}
            />
        </Page>


    }
}



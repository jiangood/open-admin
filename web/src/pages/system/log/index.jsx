import React, {Fragment} from 'react';
import {Form, Input, Tag, Typography} from "antd";
import {Ellipsis, FieldDateRange, HttpUtil, ProTable} from "@tmgg/tmgg-base";


export default class extends React.Component {


    columns = [
        {
            title: '操作',
            dataIndex: 'operation',
        },
        {
            title: '操作者',
            dataIndex: 'username'
        },

        {
            title: 'ip',
            dataIndex: 'ip',
        },


        {
            title: '时间',
            dataIndex: 'operationTime',
            sorter: true,
        },
        {
            title: '参数',
            dataIndex: 'params',
            render(v) {
                return <Ellipsis pre={true}>{v}</Ellipsis>
            }

        },
        {
            title: '结果',
            dataIndex: 'success',
            render(v) {
                return <>
                    <Tag color={v ? 'green' : 'red'}>{v ? '成功' : '失败'}</Tag>
                </>
            }
        },
        {
            title: '错误消息',
            dataIndex: 'error',
            render(v) {
                return <Ellipsis>{v}</Ellipsis>
            }
        },
    ];


    render() {
        return <>
            <ProTable
                request={(params) => HttpUtil.pageData('sysLog/page', params)}
                columns={this.columns}
            >
                <Form.Item label='操作' name='operation'>
                    <Input/>
                </Form.Item>
                <Form.Item label='时间' name='dateRange'>
                    <FieldDateRange format={"YYYY"}/>
                </Form.Item>
            </ProTable>

        </>
    }


}




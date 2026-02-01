import React, {Fragment} from 'react';
import {Form, Input, Tag} from "antd";
import {ViewText, FieldDateRange, HttpUtils, ProTable} from "../../../framework";

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
                return <ViewText value={v} ellipsis={true} />
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
                return <ViewText value={v} ellipsis={true} />
            }
        },
    ];


    render() {
        return <>
            <ProTable
                request={(params) => HttpUtils.get('admin/sysLog/page', params)}
                columns={this.columns}
            >
                <Form.Item label='操作' name='operation'>
                    <Input/>
                </Form.Item>
                <Form.Item label='时间' name='dateRange'>
                    <FieldDateRange/>
                </Form.Item>
            </ProTable>

        </>
    }


}




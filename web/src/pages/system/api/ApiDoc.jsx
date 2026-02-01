import React from "react";
import {FieldRemoteSelect, HttpUtils, PageUtils} from "../../../framework";
import {Button, Descriptions, message, Space, Table, Typography} from "antd";

const {Title, Paragraph, Text, Link} = Typography;

export class ApiDoc extends React.Component {

    state = {
        url: null,
        appId: null,
        apiList: [],
        frameworkVersion: null,
        errorList: []
    }

    columns = [
        {dataIndex: 'name', title: '名称', width: 150},
        {dataIndex: 'type', title: '类型', width: 100},
        {
            dataIndex: 'required', title: '必填', width: 100, render: v => {
                if (v == null) {
                    return null;
                }
                return v ? '是' : '否';
            }
        },
        {dataIndex: 'desc', title: '描述'},
    ]

    componentDidMount() {
        const id = PageUtils.currentParams().id
        this.loadData(id);

        let url = window.location.protocol + '//' + window.location.host
        this.setState({url})
    }

    async loadData(id) {
        const hide = message.loading('加载中...', 0)
        const rs = await HttpUtils.get('admin/apiAccount/docInfo', {id})
        this.setState(rs)
        hide()
    }


    render() {
        const {apiList} = this.state
        return <div>

            <Space>
                <FieldRemoteSelect url='admin/apiAccount/accountOptions' placeholder='请选择账号'
                                   onChange={v => this.loadData(v)}/>
                <Button type='primary'>导出PDF</Button>
            </Space>
            <div id='doc-content'>
                <Title level={1}>接口说明文档</Title>

                <Title level={2}>一、基本信息</Title>
                <Paragraph>
                    <Descriptions column={1} bordered size='small'>
                        <Descriptions.Item label='请求地址'>
                            {this.state.url}/api/gateway/接口名称
                        </Descriptions.Item>
                        <Descriptions.Item label='appId'>
                            私发
                        </Descriptions.Item>
                        <Descriptions.Item label='appSecret'>
                            私发
                        </Descriptions.Item>
                    </Descriptions>

                </Paragraph>


                <Paragraph>
                    <Typography.Text>
                        <div>
                            请求使用HTTP POST发送,请求参数也使用json格式
                        </div>
                        <div> 响应报文以JSON方式返回</div>
                    </Typography.Text>
                </Paragraph>


                <Title level={4}>请求头</Title>

                <Table rowKey='name' columns={this.columns} bordered dataSource={[
                    {name: 'appId', type: 'String', required: true, desc: '账号标识,appId'},
                    {name: 'timestamp', type: 'String', required: true, desc: '时间戳，当前UNIX时间戳，13位，精确到毫秒'},
                    {
                        name: 'sign',
                        type: 'String',
                        required: true,
                        desc: '数据签名，appId + appSecret + timestamp拼接后，进行md5摘要，值为32位小写'
                    },
                ]} size='small' pagination={false}>
                </Table>


                <Typography.Title level={4}>公共错误码</Typography.Title>
                <Table columns={[
                    {dataIndex: 'code', title: '错误码'},
                    {dataIndex: 'message', title: '错误描述'}
                ]} rowKey='code' bordered dataSource={this.state.errorList} size='small' pagination={false}>
                </Table>

                <Typography.Title level={2}>二、接口列表</Typography.Title>
                {apiList.map((api, index) => {
                    return <div key={index}>
                        <Typography.Title level={3}>{api.name} </Typography.Title>
                        <p>接口名称： {api.action}</p>
                        <p>功能描述：{api.desc}</p>


                        <Title level={5}>请求参数</Title>
                        <Table rowKey='name' columns={this.columns} bordered dataSource={api.parameterList}
                               size='small' pagination={false}>
                        </Table>

                        <Title level={5}>响应数据</Title>
                        <Table columns={this.columns} bordered
                               rowKey={'name'}
                               dataSource={[
                                   {name: 'code', type: 'int', required: true, desc: '响应码，0表示成功'},
                                   {name: 'message', type: 'String', required: false, desc: '结果提示信息'},
                                   {name: 'data', type: api.returnType, required: false, desc: '返回数据'}
                               ]} size='small' pagination={false}>
                        </Table>
                        {api.returnList != null && api.returnList.length > 0 && <>
                            <Title level={5}>data 对象 {api.returnType} 说明</Title>

                            <Table columns={this.columns}
                                   rowKey={'name'}
                                   bordered
                                   dataSource={api.returnList} size='small' pagination={false}>
                            </Table>
                        </>
                        }

                    </div>
                })}


            </div>
        </div>
    }
}

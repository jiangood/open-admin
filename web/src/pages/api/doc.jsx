import React from "react";
import {HttpUtil, PageUtil} from "@tmgg/tmgg-base";
import {Button, Descriptions, Table, Typography} from "antd";

const {Title, Paragraph, Text, Link} = Typography;

export default class extends React.Component {

    state = {
        url: null,
        appId: null,
        apiList: [],
        frameworkVersion: null,
        errorList:[]
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
        const id = PageUtil.currentParams().id

        HttpUtil.get('apiAccount/docInfo', {id}).then(rs => {
            this.setState(rs)
        })

        let url = window.location.protocol + '//' + window.location.host
        this.setState({url})
    }

    print() {
        window.print();
    }

    render() {
        const {apiList} = this.state
        return <div style={{padding: 24}}>
            <Button type='primary' onClick={this.print} className='no-print'>打印文档</Button>
            <div id='doc-content'>
                <Title level={1}>开发接口说明文档</Title>

                <Title level={2}>接口数据</Title>
                <Paragraph>
                    <Descriptions column={1} bordered size='small'>
                        <Descriptions.Item label='请求地址'>
                            {this.state.url}/api/gateway/路径
                        </Descriptions.Item>
                        <Descriptions.Item label='appId'>
                            {this.state.appId}
                        </Descriptions.Item>
                        <Descriptions.Item label='appSecret'>
                            ****** (私发给相应)
                        </Descriptions.Item>
                    </Descriptions>

                </Paragraph>


                <Title level={2}>1 接口说明</Title>
                <Paragraph>
                    <Typography.Text>
                        <div>
                            请求使用HTTP POST发送,请求参数也使用json格式
                        </div>
                        <div> 响应报文以JSON方式返回</div>
                    </Typography.Text>
                </Paragraph>


                <Title level={4}>请求公共请求头说明</Title>

                <Table columns={this.columns} bordered dataSource={[
                    {name: 'appId', type: 'String', required: true, desc: '账号标识,appId'},
                    {name: 'timestamp', type: 'String', required: true, desc: '时间戳，当前UNIX时间戳，13位，精确到毫秒'},
                    {name: 'sign', type: 'String', required: true, desc: '数据签名，appId + appSecret + timestamp拼接后，进行md5摘要，值为32位小写'},
                ]} size='small' pagination={false}>
                </Table>

                <Title level={4}>返回公共参数说明 </Title>
                <Table columns={this.columns} bordered dataSource={[
                    {name: 'code', type: 'int', required: true, desc: '返回码,成功返回0，其他表示操作错误'},
                    {name: 'message', type: 'String', required: false, desc: '结果提示信息'},
                    {name: 'data', type: 'String', required: false, desc: '返回数据JSON'}
                ]} size='small' pagination={false}>
                </Table>



                <Typography.Title level={2}>2 接口列表</Typography.Title>
                {apiList.map((api, index) => {
                    return <>
                        <Typography.Title level={3}>{'2.' + (index + 1) + " " + api.name} </Typography.Title>
                        <p>功能描述：{api.desc}</p>
                        <p>请求路径： /api/gateway/{api.action}</p>


                        <Title level={5}>请求参数说明</Title>
                        <Table columns={this.columns} bordered dataSource={api.parameterList}
                               size='small' pagination={false}>
                        </Table>

                        <Title level={5}>返回参数说明</Title>
                        <Typography.Text>
                            返回对象：{api.returnType}
                        </Typography.Text>

                        {api.returnList != null && api.returnList.length > 0 &&
                            <Table columns={this.columns} bordered
                                   dataSource={api.returnList} size='small' pagination={false}>
                            </Table>}

                    </>
                })}

                <Typography.Title level={2}>3 公共错误码</Typography.Title>
                <Table columns={[
                    {dataIndex:'code',title:'错误码'},
                    {dataIndex:'message', title:'错误描述'}
                ]} rowKey='code' bordered dataSource={this.state.errorList} size='small' pagination={false}>
                </Table>
            </div>
        </div>
    }
}

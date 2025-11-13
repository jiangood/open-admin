import React from 'react'
import {Button, Descriptions, Space} from "antd";
import {ButtonList, HttpUtil} from "@/framework";
import {ProTable} from "@/framework";
export default class extends React.Component {

    state = {
        status: {}
    }


    render() {
        return <div>

            <ProTable
                toolBarRender={() => <Button type='primary'
                                             target='_blank'
                                             href={"/ureport/designer"}>打开设计器</Button>}

                request={(params) => HttpUtil.pageData('admin/ureport/page', params)}
                columns={[
                    {title: "文件名称", dataIndex: 'name'},
                    {title: "存储器", dataIndex: 'providerName'},
                    {title: "存储器前缀", dataIndex: 'providerPrefix'},
                    {title: "更新时间", dataIndex: 'updateDate'},
                    {title: "-", dataIndex: 'option',
                        render(_,record){
                            return <ButtonList >
                                <a href={record.previewUrl} target='_blank'>预览</a>
                                <a href={record.designerUrl} target='_blank'>设计</a>
                            </ButtonList>
                        }},
                ]}></ProTable>


        </div>
    }


}




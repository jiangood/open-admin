import React from "react";
import {Card, Empty, Modal, Skeleton, Table, Tabs, Typography} from "antd";

import {FormOutlined, ShareAltOutlined} from "@ant-design/icons";
import {FormRegistryUtils, Gap, HttpUtils} from "../../framework";

/**
 * 查看流程处理情况（不含表单）
 */
export class ViewFlowableInstanceProgress extends React.Component {

    state = {
        instanceCommentList: [],
        vars: {},

        id: null,
        starter: null,
        startTime: null,
        name: null,

        data: {
            commentList: [],
            img: null
        },
        loading: true,

        errorMsg: null
    }


    componentDidMount() {
        const {value} = this.props;
        HttpUtils.get("admin/flowable/my/getInstanceInfo", {businessKey: value}).then(rs => {
            this.setState(rs)
            this.setState({data: rs})
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })

    }

    onImgClick = () => {
        const {data} = this.state

        const {img} = data
        Modal.info({
            title: '流程图',
            width: '70vw',
            content: <div style={{width: '100%', overflow: 'auto', maxHeight: '80vh'}}><img src={img}/></div>
        })
    };

    render() {
        if (this.state.errorMsg) {
            return <Empty description={this.state.errorMsg}></Empty>
        }

        const {data, loading} = this.state
        const {commentList, img} = data
        if (loading) {
            return <Skeleton/>
        }


        return <>
            <Typography.Title level={4}>{data.name}</Typography.Title>
            <Typography.Text type="secondary">{data.starter} &nbsp;&nbsp; {data.startTime}</Typography.Text>
            <Gap></Gap>
            <img src={img} style={{maxWidth: '100%'}}
                 onClick={this.onImgClick}/>
            <Gap></Gap>
            <Table dataSource={commentList}

                   size='small'
                   pagination={false}
                   rowKey='id'
                   columns={[
                       {
                           dataIndex: 'content',
                           title: '操作'
                       },
                       {
                           dataIndex: 'user',
                           title: '处理人'
                       },
                       {
                           dataIndex: 'time',
                           title: '处理时间'
                       },
                   ]}
            />
        </>

    }

}

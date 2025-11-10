import React from "react";
import {Card, Empty, Skeleton, Table} from "antd";
import {HttpUtil} from "@tmgg/tmgg-base";


export default class InstanceStatusInfo extends React.Component {

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
        const {id, businessKey} = this.props;
        HttpUtil.get("flowable/userClient/getInstanceInfo", {id, businessKey}).then(rs => {
            this.setState(rs)
            this.setState({data: rs})
        }).catch(e => {
            this.setState({errorMsg: e})
        }).finally(() => {
            this.setState({loading: false})
        })

    }


    render() {
        if (this.state.errorMsg) {
            return <Empty description={this.state.errorMsg}></Empty>
        }

        const {data, loading} = this.state
        const {commentList, img} = data
        if (loading) {
            return <Skeleton/>
        }


        return <Card title='处理记录'>
            <img src={img} style={{maxWidth: '100%'}}/>
            <Table dataSource={commentList}
                   bordered
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
        </Card>

    }

}

import React from "react";
import {Form, Input, Typography} from "antd";
import {PageUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {


    render() {
        console.log('测试表单', this.props)
        return <div>
            <Typography.Title level={3} >测试表单</Typography.Title>
            <Form labelCol={{flex:'150px'}}>
                <Form.Item name='days' label='请假天数'>
                    <Input disabled/>
                </Form.Item>

                <Form.Item name='pathname' label='pathname' initialValue={this.props.location.pathname}>
                    <Input disabled/>
                </Form.Item>

                <Form.Item name='search' label='search' initialValue={this.props.location.search}>
                    <Input disabled/>
                </Form.Item>
                <Form.Item name='params' label='路由参数' initialValue={JSON.stringify(this.props.location.params)}>
                    <Input disabled/>
                </Form.Item>
            </Form>


        </div>
    }
}

import React from "react";
import {Alert, Col, Row, Table} from "antd";
import {HttpUtil, Page} from "@tmgg/tmgg-base";
import * as echarts from 'echarts';

export default class extends React.Component {

    state = {
        dataSource: [],
        summary: null
    }

    componentDidMount() {
        HttpUtil.get('jobStatus/info').then(rs => {
            this.setState({dataSource: rs.list, summary: rs.summary})
        })

        HttpUtil.get("jobStatus/statsTotal").then(list => {

            const dates = list.map(item => item.date);
            const success = list.map(item => item.success);
            const error = list.map(item => item.error)


            var myChart = echarts.init(document.getElementById('main'));
            myChart.setOption({
                title: {
                    text: '作业执行趋势图'
                },
                tooltip: {},
                xAxis: {
                    data: dates
                },
                yAxis: {},
                series: [
                    {
                        name: '成功',
                        type: 'bar',
                        data: success,
                        itemStyle: {
                            color: 'green'
                        }
                    },
                    {
                        name: '失败',
                        type: 'bar',
                        data: error,
                        itemStyle: {
                            color: 'red'
                        }
                    }
                ]
            });
        })


    }

    render() {
        return <Page padding>


            <Alert message={<pre>{this.state.summary}</pre>} style={{marginBottom: 12}}></Alert>
            <div id='main' style={{height: 300}}></div>


            <Table
                title={() => <div>正在执行的Job</div>}
                bordered
                rowKey='id'
                pagination={false}
                dataSource={this.state.dataSource}
                columns={[
                    {title: '实例ID', dataIndex: 'id'},
                    {title: 'JobKey', dataIndex: 'jobKey'},
                    {title: 'TriggerKey', dataIndex: 'triggerKey'},
                    {title: '执行类', dataIndex: 'className'},
                    {title: '触发时间', dataIndex: 'fireTime'},
                    {title: '下次触发', dataIndex: 'nextFireTime'},

                ]}></Table>
        </Page>
    }
}

import React from "react";
import {Card, Tabs} from "antd";
import AllTask from "../../components/monitor/AllTask";
import AllDefinition from "../../components/monitor/AllDefinition";
import AllInstance from "../../components/monitor/AllInstance";
import {Page} from "@/framework";

export default class extends React.Component {

  render() {
    const items = [
      { label: '运行中的流程', key: 'AllInstance', children: <AllInstance /> },
      { label: '运行中的任务', key: 'AllTask', children: <AllTask /> },
      { label: '所有定义', key: 'AllDefinition', children: <AllDefinition /> },
    ];
    return <Page padding>  <Tabs items={items} destroyOnHidden /></Page>

  }
}

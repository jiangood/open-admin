import React from "react";
import {Card, Tabs} from "antd";
import AllTask from "../../components/monitor/AllTask";
import AllDefinition from "../../components/monitor/AllDefinition";
import AllInstance from "../../components/monitor/AllInstance";
import {Page} from "@tmgg/tmgg-base";

export default class extends React.Component {

  render() {
    const items = [
      { label: '所有流程', key: 'AllInstance', children: <AllInstance /> },
      { label: '所有任务', key: 'AllTask', children: <AllTask /> },
      { label: '所有定义', key: 'AllDefinition', children: <AllDefinition /> },
    ];
    return <Page padding>  <Tabs items={items} destroyOnHidden /></Page>

  }
}

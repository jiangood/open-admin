import React from "react";
import {Button, Card, Space} from "antd";
import {MsgBox} from "../components/MsgBox";


export default class extends React.Component {

  state = {
  }

  render() {

    return <Card>
      欢迎使用本系统

      <Space>
        <Button onClick={()=>MsgBox.alert('你好')}>alert</Button>
        <Button onClick={()=>MsgBox.confirm('你好')}>comfirm</Button>
        <Button onClick={()=>MsgBox.prompt('你好')}>prompt</Button>

      </Space>

    </Card>
  }

}

import React from "react";
import './index.less'
import {Card, Empty, Spin} from "antd";
export class Panel extends React.Component{

    render() {
       return <Card title={this.props.title} size='small'>
           {this.renderContent()}
       </Card>
    }

    renderContent(){
        if(this.props.loading){
            return <Spin />
        }
        if(this.props.empty){
            return  <Empty />
        }

        return this.props.children || <Empty />
    }

}

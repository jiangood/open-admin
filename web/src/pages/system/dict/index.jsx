import React from "react";
import Dict from "./Dict";
import DictItem from "./DictItem";
import {Splitter} from "antd";

export default class extends React.Component {

    state = {
        selectedKey: null
    }


    render() {
        return <Splitter>
            <Splitter.Panel defaultSize={600} style={{paddingRight:16}}>
                <Dict onChange={selectedKey => this.setState({selectedKey})}/>
            </Splitter.Panel>
            <Splitter.Panel style={{paddingLeft:16}}>
                <DictItem sysDictId={this.state.selectedKey}/>
            </Splitter.Panel>
        </Splitter>


    }
}

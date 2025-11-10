import React from "react";
import './index.less'
import {theme} from "@tmgg/tmgg-commons-lang";

/**
 * 上下间隔
 */
export class Page extends React.Component {

    static defaultProps = {
        padding: false,
        backgroundGray:false
    }

    render() {
        const style = {}
        if(this.props.padding){
            style.padding = 16
        }
        if(this.props.backgroundGray){
            style.backgroundColor = theme["background-color"]
        }


        return <div className={'tmgg-page'}  style={style}>
            {this.props.children}
        </div>
    }

}

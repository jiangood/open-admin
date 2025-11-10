import React from "react";
import {StrUtil} from "@tmgg/tmgg-commons-lang";

export class ViewRange extends React.Component {

    static  defaultProps = {
         min : '未知',max : '未知'
    }

    render() {
        let {min,max}  = this.props
        if(min == null && max == null){
            return null;
        }
        min = min == null ? '未知': min;
        max = max == null ? '未知': max;

        return min + ' - ' + max;

    }
}

import React from "react";
import './index.less'

/**
 * 上下间隔
 */
export class Gap extends React.Component {

    render() {
        const {n =0} = this.props;
        let height = 16 +  8 * n

        return <div className='tmgg-gap' style={{height}}></div>
    }
}

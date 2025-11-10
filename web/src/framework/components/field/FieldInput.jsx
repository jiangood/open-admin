import React from 'react';
import {Input, Radio} from 'antd';



export class FieldInput extends React.Component {
    render() {
        if(this.props.mode === 'read'){
            return this.props.value
        }

        return  <Input value={this.props.value} onChange={this.props.onChange} />
    }
}

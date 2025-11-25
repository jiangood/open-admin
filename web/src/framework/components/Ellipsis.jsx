import React from 'react';
import {Modal} from 'antd';
import './Ellipsis.less'
import {StringUtils} from "../utils";

export class Ellipsis extends React.Component {
  static defaultProps = {length: 15}

  render() {
    let {length, children} = this.props;
    let text = children;
    if(!text){
      return
    }
    if(text.length < this.props.length){
      return text;
    }


    const shortText =  StringUtils.ellipsis(text, length)
    return (
        <span className='ellipsis-text' onClick={this.showFull}>{shortText}</span>
    );
  }

  showFull = () => {
    const pre = this.props.pre || false
    let node = this.props.children
    if(pre){
      node = <pre>{node}</pre>
    }
    Modal.info({
      icon: null,
      title:'文本内容',
      content: node
    })

  };
}

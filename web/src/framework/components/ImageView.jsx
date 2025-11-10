import React from 'react';
import { Modal } from 'antd';
import {SysUtil} from "../system";


/**
 *  可以使用 fileId 或 url 属性
 *
 *    // 推荐使用 ， 支持id， url， 并支持逗号分隔符
 *   value?: string | string[];
 *
 *
 */
export class ImageView extends React.Component {
  preview = (url) => {
    Modal.info({
      title: '预览图片',
      width: '70vw',
      content: <img src={url} width="100%" />,
    });
  };

  render() {
    let vs = this.props.id

    if (!vs) {
      return;
    }

    if (typeof vs === 'string') {
      vs = vs.split(',');
    }

    let urlList = [];
    for (let v of vs) {
      let isId = v.indexOf('/') === -1;
      let isAbsUrl = v.startsWith('http');
      if (isAbsUrl) {
        urlList.push(v);
        continue;
      }

      if (isId) {
        urlList.push(SysUtil.getServerUrl() + 'sysFile/preview/' + v);
        continue;
      }

      urlList.push(SysUtil.getServerUrl() + v);
    }

    const imgs = urlList.map((url) => (
      <img
        style={{ display: 'inline-block' }}
        key={url}
        src={url}
        onClick={() => this.preview(url)}
        width={60}
        height={60}
      />
    ));

    return imgs;
  }
}

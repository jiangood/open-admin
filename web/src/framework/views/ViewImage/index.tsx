import React from 'react';
import {Modal} from 'antd';
import {UrlUtils} from '../../utils';

/**
 * 图片展示组件
 *
 * 支持的属性：
 * - value: 图片地址，支持多个（逗号分隔或数组）。objectName（public/、private/ 开头）自动拼 /file/ 前缀
 * - size: 缩略图尺寸（正方形边长），默认 60
 * - borderRadius: 圆角，默认 4
 * - preview: 是否可点击放大预览，默认 true
 * - previewTitle: 预览弹窗标题，默认「预览图片」
 * - placeholder: value 为空时展示的内容，默认不渲染
 * - style: 附加到 img 的样式
 */
export class ViewImage extends React.Component {

  state = {
    modalOpen: false,
    previewUrl: null,
  };

  render() {
    const {value, size = 60, borderRadius = 4, preview = true, previewTitle = '预览图片',
           placeholder, style} = this.props;

    if (!value) {
      return placeholder || null;
    }

    let vs = value;
    if (typeof vs === 'string') {
      vs = vs.split(',');
    }

    const urlList = [];
    const thumbList = [];
    for (let v of vs) {
      const isAbsUrl = v.startsWith('http');
      const isDataUrl = v.startsWith('data:');
      const isObjectName = v.startsWith('public/') || v.startsWith('private/');
      if (isAbsUrl || isDataUrl) {
        urlList.push(v);
        thumbList.push(v);
        continue;
      }

      if (isObjectName) {
        urlList.push(UrlUtils.contextPath('/file/' + v));
        thumbList.push(UrlUtils.contextPath('/file/' + v + '?thumb=1'));
        continue;
      }

      urlList.push(UrlUtils.contextPath(v));
      thumbList.push(UrlUtils.contextPath(v));
    }

    const closeModal = () => this.setState({modalOpen: false});

    const imgs = thumbList.map((thumb, i) => (
      <img
        key={urlList[i]}
        src={thumb}
        width={size}
        height={size}
        style={{display: 'inline-block', objectFit: 'cover', borderRadius, cursor: preview ? 'pointer' : undefined, ...style}}
        onClick={() => {
          if (preview) {
            this.setState({modalOpen: true, previewUrl: urlList[i]})
          }
        }}
      />
    ));

    return (
      <>
        {imgs}
        <Modal open={this.state.modalOpen} title={previewTitle} width="70vw" footer={null}
               onCancel={closeModal}>
          <div style={{maxHeight: '70vh', overflow: 'auto'}}>
            <img src={this.state.previewUrl} style={{maxWidth: '100%'}}/>
          </div>
        </Modal>
      </>
    );
  }
}

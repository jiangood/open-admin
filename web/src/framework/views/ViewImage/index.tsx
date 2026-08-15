import React from 'react';
import {Image} from 'antd';
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

  static readonly previewGroupProps = {
    preview: {
      scaleStep: 0.3,
    },
  };

  resolveUrl = (v) => {
    const isAbsUrl = v.startsWith('http');
    const isDataUrl = v.startsWith('data:');
    const isObjectName = v.startsWith('public/') || v.startsWith('private/');
    if (isAbsUrl || isDataUrl) {
      return {url: v, thumb: v};
    }
    if (isObjectName) {
      return {
        url: UrlUtils.contextPath('/file/' + v),
        thumb: UrlUtils.contextPath('/file/' + v + '?thumb=1'),
      };
    }
    return {url: UrlUtils.contextPath(v), thumb: UrlUtils.contextPath(v)};
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

    const items = vs.map(this.resolveUrl);
    const previewConfig = {
      scaleStep: 0.3,
    };
    if (previewTitle) {
      previewConfig.title = previewTitle;
    }

    return (
      <Image.PreviewGroup preview={previewConfig}>
        {items.map((item) => (
          <Image
            key={item.url}
            src={item.thumb}
            width={size}
            height={size}
            style={{display: 'inline-block', objectFit: 'cover', borderRadius, cursor: preview ? 'pointer' : undefined, ...style}}
            preview={preview ? {src: item.url} : false}
          />
        ))}
      </Image.PreviewGroup>
    );
  }
}

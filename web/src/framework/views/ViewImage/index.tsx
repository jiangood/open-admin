import React from 'react';
import {Modal} from 'antd';
import {UrlUtils} from '../../utils';


export class ViewImage extends React.Component {

  state = {
    modalOpen: false,
    previewUrl: null,
  };

  render() {
    let vs = this.props.value

    if (!vs) {
      return;
    }

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
        style={{ display: 'inline-block' }}
        key={urlList[i]}
        src={thumb}
        onClick={() => this.setState({modalOpen: true, previewUrl: urlList[i]})}
        width={60}
        height={60}
      />
    ));

    return (
      <>
        {imgs}
        <Modal open={this.state.modalOpen} title="预览图片" width="70vw" footer={null}
               onCancel={closeModal}>
          <div style={{maxHeight:'70vh',overflow:'auto'}}>
            <img src={this.state.previewUrl} style={{maxWidth: '100%'}}/>
          </div>
        </Modal>
      </>
    );
  }
}

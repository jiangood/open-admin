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
    for (let v of vs) {
      const isId = v.indexOf('/') === -1;
      const isAbsUrl = v.startsWith('http');
      const isDataUrl = v.startsWith('data:');
      if (isAbsUrl || isDataUrl) {
        urlList.push(v);
        continue;
      }

      if (isId) {
        urlList.push(UrlUtils.contextPath('/admin/sysFile/preview/' + v));
        continue;
      }

      urlList.push(UrlUtils.contextPath(v));
    }

    const closeModal = () => this.setState({modalOpen: false});

    const imgs = urlList.map((url) => (
      <img
        style={{ display: 'inline-block' }}
        key={url}
        src={url}
        onClick={() => this.setState({modalOpen: true, previewUrl: url})}
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

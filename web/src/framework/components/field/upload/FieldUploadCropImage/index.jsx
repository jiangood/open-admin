import {Upload} from 'antd';

import React from 'react';
import ImgCrop from 'antd-img-crop';
import {PlusOutlined} from '@ant-design/icons';
import {SysUtil} from "../../../../system";
import {FieldUploadFile} from "../FieldUploadFile";


export class FieldUploadCropImage extends React.Component {
  render() {
      return <FieldUploadFile {...this.props} accept={"image/*"} cropImage={true} />
  }
}



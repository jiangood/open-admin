import { Modal, Upload } from 'antd';

import React from 'react';
import { PlusOutlined } from '@ant-design/icons';
import {SysUtil} from "../../../system";
import {FieldUploadFile} from "./FieldUploadFile";


/**
 * crop: 带裁切
 */

export class FieldUploadImage extends React.Component {

  render() {
    return <FieldUploadFile {...this.props} accept={'image/*'} />
  }
}



import React from "react";
import ImgCrop from "antd-img-crop";
import {Modal, Upload} from "antd";
import UploadOutlined from "@ant-design/icons/lib/icons/UploadOutlined";
import {ViewFile} from "../../views";
import {ObjectUtils} from "../../utils";

/**
 * 上传图片前裁切， 单张图片
 *
 * 可参考 react-easy-crop
 */
export class FieldUploadFile extends React.Component {

    state = {
        // 传入的参数
        maxCount: 1,
        cropImage: false,

        // 内部参数
        fileList: [],
        value: null, // 都好分隔的文件id
    };

    constructor(props) {
        super(props);
        ObjectUtils.copyPropertyIfPresent(props, this.state);
        this.state.fileList = this.convertInputToComponentValue(this.state.value);
    }

    convertInputToComponentValue(value) {
        const list = [];
        if (value && value.length > 0) {
            const arr = value.split(",");
            for (const id of arr) {
                const url = 'admin/sysFile/preview/' + id;
                const file = {id, url, uid: id, name: id, status: 'done', fileName: id};
                list.push(file);
            }
        }

        return list;
    }

    convertComponentValueToOutput(fileList) {
        const fileIds = [];
        for (const f of fileList) {
            if (f.status === 'done') {
                if (f.response) { // 新上传的
                    const ajaxResult = f.response;
                    if (ajaxResult.success) {
                        const {id, name} = ajaxResult.data;
                        f.id = id;
                        fileIds.push(id);
                    } else {
                        Modal.error({title: '上传文件失败', content: ajaxResult.message});
                    }
                } else { // 老的
                    fileIds.push(f.id);
                }
            }
        }
        return fileIds;
    }

    handleChange = ({fileList, event, file}) => {
        console.log('检测到文件变化', fileList);
        const rs = file.response;
        if (rs != null && rs.success === false) {
            Modal.error({
                title: '上传失败',
                content: rs.message,
            });
            return;
        }

        this.setState({fileList});

        const newIds = this.convertComponentValueToOutput(fileList);
        if (newIds.length > 0 && this.props.onFileChange) {
            this.props.onFileChange(fileList);
        }
        if (this.props.onChange) {
            const value = newIds.join(',');
            console.log('上传文件值', value);
            this.props.onChange(value);
        }

    };

    handlePreview = (file) => {
        Modal.info({
            title: '文件预览',
            width: '80vw',
            content: <ViewFile value={file.id} height='70vh'/>
        });

    };

    render() {
        if (this.state.cropImage) {
            return <ImgCrop cropperProps={this.props.cropperProps} modalTitle={'裁剪图片'} fillColor={null}>
                {this.getUpload()}
            </ImgCrop>;
        }

        return this.getUpload();
    }

    getUpload = () => {
        const {accept, fileList, maxCount} = this.state;

        return <Upload
            action={'admin/sysFile/upload'}
            listType={this.props.listType || 'picture-card'}
            fileList={fileList}
            onChange={this.handleChange}
            multiple={false}
            accept={accept}
            maxCount={maxCount}
            onPreview={this.handlePreview}
        >
            {this.renderButton()}

        </Upload>;
    };

    renderButton = () => {
        const {fileList, maxCount} = this.state;
        if (fileList.length >= maxCount) {
            return null;
        }

        return <>
            <UploadOutlined/>
            <div className="ant-upload-text">选择文件</div>
        </>;
    };
}
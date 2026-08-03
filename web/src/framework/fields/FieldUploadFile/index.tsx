import React from "react";
import ImgCrop from "antd-img-crop";
import type {ImgCropProps} from "antd-img-crop";
import {message, Modal, Upload} from "antd";
import type {UploadChangeParam, UploadFile, UploadProps} from "antd";
import { UploadOutlined } from "@ant-design/icons";
import {ViewFile} from "../../views";
import {ObjectUtils, UrlUtils} from "../../utils";
import type {FieldProps} from '../types';

/** 框架内使用的上传文件对象，额外携带 sysFile 的 objectName */
export type SysUploadFile = UploadFile & { objectName?: string };

export interface FieldUploadFileProps extends FieldProps<string> {
    /** 最大上传数量，默认 1 */
    maxCount?: number;
    /** 是否启用上传前裁切图片 */
    cropImage?: boolean;
    /** 裁切配置（antd-img-crop 的 cropperProps） */
    cropperProps?: ImgCropProps['cropperProps'];
    /** 上传列表的内建样式，默认 picture-card */
    listType?: UploadProps['listType'];
    /** 接受的文件类型，如 image/* */
    accept?: string;
    /** 文件可见性，默认 public */
    visibility?: 'public' | 'private';
    /** 文件列表变化回调（新增文件上传成功后才触发） */
    onFileChange?: (fileList: SysUploadFile[]) => void;
}

interface FieldUploadFileState {
    maxCount: number;
    cropImage: boolean;
    fileList: SysUploadFile[];
    /** 逗号分隔的文件 objectName */
    value: string | null;
    accept?: string;
    visibility?: 'public' | 'private';
}

/**
 * 上传图片前裁切， 单张图片
 *
 * 可参考 react-easy-crop
 */
export class FieldUploadFile extends React.Component<FieldUploadFileProps, FieldUploadFileState & { errorTitle?: string; errorContent?: string; previewObjectName?: string }> {

    state: FieldUploadFileState & { errorTitle?: string; errorContent?: string; previewObjectName?: string } = {
        // 传入的参数
        maxCount: 1,
        cropImage: false,
        visibility: 'public',

        // 内部参数
        fileList: [],
        value: null, // 逗号分隔的文件objectName
    };

    constructor(props: FieldUploadFileProps) {
        super(props);
        ObjectUtils.copyPropertyIfPresent(props, this.state);
        this.state.fileList = this.convertInputToComponentValue(this.state.value);
    }

    componentDidUpdate(prevProps: FieldUploadFileProps) {
        const next: Partial<FieldUploadFileState> = {};
        if (this.props.maxCount !== prevProps.maxCount) next.maxCount = this.props.maxCount;
        if (this.props.cropImage !== prevProps.cropImage) next.cropImage = this.props.cropImage;
        if (this.props.visibility !== prevProps.visibility) next.visibility = this.props.visibility;

        const prevValue = prevProps.value ?? null;
        const curValue = this.props.value ?? null;
        if (curValue !== prevValue && curValue !== this.state.value) {
            next.fileList = this.convertInputToComponentValue(curValue);
            next.value = curValue;
        }

        if (Object.keys(next).length > 0) this.setState(next);
    }

    convertInputToComponentValue(value: string | null | undefined): SysUploadFile[] {
        const list: SysUploadFile[] = [];
        if (value && value.length > 0) {
            const arr = value.split(",");
            for (const objectName of arr) {
                const url = UrlUtils.contextPath('/file/' + objectName);
                const file = {objectName, url, uid: objectName, name: objectName, status: 'done', fileName: objectName} as SysUploadFile;
                list.push(file);
            }
        }

        return list;
    }

    convertComponentValueToOutput(fileList: SysUploadFile[]): string[] {
        const objectNames: string[] = [];
        for (const f of fileList) {
            if (f.status === 'done') {
                if (f.response) { // 新上传的
                    const ajaxResult = f.response;
                    if (ajaxResult.success) {
                        const {objectName, name} = ajaxResult.data;
                        f.objectName = objectName;
                        objectNames.push(objectName);
                    } else {
                        this.setState({errorTitle: '上传文件失败', errorContent: ajaxResult.message});
                    }
                } else { // 老的
                    objectNames.push(f.objectName as string);
                }
            }
        }
        return objectNames;
    }

    handleChange = ({fileList, event, file}: UploadChangeParam<SysUploadFile>) => {
        const rs = file.response;
        if (rs != null && rs.success === false) {
            this.setState({errorTitle: '上传失败', errorContent: rs.message});
            return;
        }

        if (file.status === 'done' && rs?.success) {
            message.success(`文件「${rs.data?.name || ''}」上传成功`);
        }

        const newIds = this.convertComponentValueToOutput(fileList);
        const value = newIds.join(',');
        this.setState({fileList, value});

        if (newIds.length > 0 && this.props.onFileChange) {
            this.props.onFileChange(fileList);
        }
        if (this.props.onChange) {
            this.props.onChange(value);
        }

    };

    handlePreview = (file) => {
        this.setState({previewObjectName: file.objectName});
    };

    render() {
        if (this.state.cropImage) {
            return <ImgCrop cropperProps={this.props.cropperProps} modalTitle={'裁剪图片'} fillColor={null}>
                {this.getUpload()}
            </ImgCrop>;
        }

        return <>
            {this.getUpload()}
            <Modal open={!!this.state.errorTitle} title={this.state.errorTitle} okText="确定"
                   onCancel={() => this.setState({errorTitle: undefined})}
                   onOk={() => this.setState({errorTitle: undefined})}>
                {this.state.errorContent}
            </Modal>
            <Modal open={!!this.state.previewObjectName} title="文件预览" width="80vw" footer={null}
                   onCancel={() => this.setState({previewObjectName: undefined})}>
                {this.state.previewObjectName && <ViewFile value={this.state.previewObjectName} height='70vh'/>}
            </Modal>
        </>
    }

    getUpload = () => {
        const {accept, fileList, maxCount} = this.state;

        return <Upload
            action={UrlUtils.contextPath('/admin/sysFile/upload')}
            data={{visibility: this.state.visibility}}
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
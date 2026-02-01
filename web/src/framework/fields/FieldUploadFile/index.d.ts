import React from "react";
import {CropperProps} from "react-easy-crop/Cropper";
import {UploadListType} from "antd/es/upload/interface";
import { FieldProps } from '../types';

/**
 * 上传图片前裁切， 单张图片
 *
 * 可参考 react-easy-crop
 */
interface FieldUploadFileProps extends FieldProps<string> {
    onFileChange?: (fileList: any[]) => void;

    /**
     * 是否裁切图片
     */
    cropImage?: boolean;
    cropperProps?: CropperProps;

    maxCount?: number;

    accept?: "image/*" | ".pdf" | ".docx" | '.xlsx' | string;

    children?: React.ReactNode;

    listType?: UploadListType;
}

export class FieldUploadFile extends React.Component<FieldUploadFileProps, string> {

}
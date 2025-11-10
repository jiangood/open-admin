// @ts-ignore
import React from "react";
// @ts-ignore
import {CropperProps} from "react-easy-crop/Cropper";


declare type FieldUploadCropImageProps = {
    value?: string; // 文件的值，sysFile的id
    onChange?: (v:string)=>{};

    cropperProps?: CropperProps;

    maxCount?: number
};

/**
 * 上传图片前裁切， 单张图片
 *
 * 可参考 react-easy-crop
 */
export class FieldUploadCropImage extends React.Component<FieldUploadCropImageProps, any> {
}

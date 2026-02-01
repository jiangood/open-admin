import React from 'react';
import { FieldProps } from '../types';

/**
 * 富文本编辑器
 *
 * 图片上传相关配置 https://www.tiny.cloud/docs/tinymce/7/image/
 */
interface FieldEditProps extends FieldProps<string> {
    height?: number;
}

export class FieldEditor extends React.Component<FieldEditProps, any> {
}
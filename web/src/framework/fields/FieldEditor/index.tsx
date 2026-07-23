import React from 'react';
import {Editor as TinyMceEditor} from '@tinymce/tinymce-react';
import {UrlUtils} from '../../utils';

/**
 * 富文本编辑器
 *
 * 图片上传相关配置 https://www.tiny.cloud/docs/tinymce/7/image/
 */
export class FieldEditor extends React.Component {
    render() {
        const uploadUrl = UrlUtils.contextPath('/admin/sysFile/upload');
        const jsUrl = UrlUtils.contextPath('/admin/tinymce/tinymce.min.js');
        const {value, onChange, height} = this.props;

        return (
            <TinyMceEditor
                initialValue={value}
                tinymceScriptSrc={jsUrl}
                init={{
                    min_height: 300,
                    language: 'zh_CN',
                    height: height,
                    images_upload_url: uploadUrl,
                    promotion: false,
                    cache_suffix: '?v=v7.7',
                    plugins: [
                        'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
                        'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
                        'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount',
                        'emoticons'
                    ],
                    image_description: false,
                    setup: function (editor) {
                        editor.on('OpenWindow', function(e) {
                            const dialog = e.dialog;
                            if (dialog && dialog.getData().dimensions) {
                                dialog.showTab("upload");
                            }
                        });
                    },
                }}
                onChange={e => {
                    if (onChange) {
                        onChange(e.target.getContent());
                    }
                }}
            />
        );
    }
}
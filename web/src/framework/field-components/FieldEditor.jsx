import React from 'react';
import {Editor as TinyMceEditor} from '@tinymce/tinymce-react';


/**
 * 富文本编辑器
 *
 * 图片上传相关配置 https://www.tiny.cloud/docs/tinymce/7/image/
 */
export class FieldEditor extends React.Component {
    render() {
        let uploadUrl = 'admin/sysFile/upload'
        let jsUrl = 'admin/tinymce/tinymce.min.js';
        const {value,onChange,height} = this.props

        return <>
            <TinyMceEditor
                initialValue={value}
                tinymceScriptSrc={jsUrl}
                init={{
                    min_height: 300,
                    language: 'zh_CN',
                    height: height,

                    // 上传图片
                    images_upload_url: uploadUrl,
                    promotion: false, // 不显示升级按钮（右上角）
                    cache_suffix: '?v=v7.7',

                    plugins: [
                        'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
                        'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
                        'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount',
                        'emoticons'
                    ],
                    image_description:false,

                    // 设置图片上传对话框默认选中上传Tab
                    setup: function (editor) {
                        editor.on('OpenWindow', function(e) {
                            var dialog = e.dialog;
                            // 包含 dimensions 属性应该就是 上传图片的对话框
                            if (dialog && dialog.getData().dimensions) {
                                dialog.showTab("upload")
                            }
                        });
                    },

                }}
                onChange={e => {
                    if (onChange) {
                        onChange(e.target.getContent())
                    }
                }}
            />
        </>;
    }
}

import {Spin} from 'antd';
import React from 'react';
import {Editor as TinyMceEditor} from '@tinymce/tinymce-react';
import {UrlUtils} from '../../utils';
import type {FieldProps} from '../types';

export interface FieldEditorProps extends FieldProps<string> {
    /** 编辑器高度，默认 300 */
    height?: number;
    /** 是否公开免登录访问，默认 true（富文本图片需公开访问） */
    isPublic?: boolean;
}

/**
 * 富文本编辑器
 *
 * 图片上传相关配置 https://www.tiny.cloud/docs/tinymce/7/image/
 */
export class FieldEditor extends React.Component<FieldEditorProps, {loading: boolean}> {
    state = {loading: false};
    private timer: number | undefined;

    componentDidMount() {
        this.timer = window.setTimeout(() => {
            this.setState({loading: true});
        }, 200);
    }

    componentWillUnmount() {
        if (this.timer != null) {
            window.clearTimeout(this.timer);
        }
    }

    render() {
        const {loading} = this.state;
        const isPublic = this.props.isPublic ?? true;
        const uploadUrl = UrlUtils.contextPath('/admin/sysFile/upload') + '?isPublic=' + isPublic;
        const jsUrl = UrlUtils.contextPath('/admin/tinymce/tinymce.min.js');
        const {value, onChange, height} = this.props;
        const editorHeight = height || 300;

        return (
            <div style={{position: 'relative', minHeight: loading ? editorHeight + 44 : undefined}}>
                <TinyMceEditor
                    value={value}
                    tinymceScriptSrc={jsUrl}
                    onInit={() => {
                        if (this.timer != null) {
                            window.clearTimeout(this.timer);
                        }
                        this.setState({loading: false});
                    }}
                    init={{
                    min_height: 300,
                    language: 'zh_CN',
                    height: height,
                    images_upload_url: uploadUrl,
                    convert_urls: false,
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
                onEditorChange={content => {
                    if (onChange) {
                        onChange(content);
                    }
                }}
                />
                {loading && (
                    <div style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        minHeight: editorHeight + 44,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 8,
                        background: '#fff',
                        zIndex: 1,
                    }}>
                        <Spin/>
                        <span style={{color: '#999'}}>编辑器加载中...</span>
                    </div>
                )}
            </div>
        );
    }
}
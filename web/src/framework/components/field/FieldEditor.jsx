import React from 'react';
import {Editor as TinyMceEditor} from '@tinymce/tinymce-react';
import {SysUtil} from "../../system";
import {UrlUtil} from "@tmgg/tmgg-commons-lang";


/**
 * 富文本编辑器
 */

export class FieldEditor extends React.Component {

  render() {
    let serverUrl = SysUtil.getServerUrl();
    let uploadUrl = UrlUtil.join(serverUrl , 'sysFile/upload')

    return <>
      <TinyMceEditor
        initialValue={this.props.value}
        tinymceScriptSrc={UrlUtil.join(serverUrl , "tinymce/tinymce.min.js")}
        init={{
          min_height: 500,
          language: 'zh_CN',
          plugins: [
            'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
            'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
            'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
          ],
            toolbar_mode:'Sliding',
        toolbar: "fontsize  blocks  bold italic forecolor backcolor | alignleft aligncenter alignright alignjustify |image link | bullist numlist outdent indent | removeformat  |fontfamily| undo redo | help",
          content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }',

          // 上传图片
          images_upload_url: uploadUrl,
          images_upload_base_path: serverUrl,
            promotion: false, // 不显示升级按钮（右上角）
            cache_suffix: '?v=v7.7'

        }}
        onChange={e => {
          if (this.props.onChange) {
            this.props.onChange(e.target.getContent())
          }
        }}
      />
    </>;
  }
}

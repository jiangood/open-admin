package io.github.jiangood.as.common.tools;

import cn.hutool.core.util.StrUtil;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

public class ContentTypeTool {

    public static boolean isVideo(String contentType) {
        return StrUtil.startWith(contentType, "video");
    }

    /**
     * 根据文件扩展名获取Content-Type字符串
     */
    public static String getContentTypeByExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        extension = extension.toLowerCase();

        switch (extension) {
            // 图片
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "gif":
                return MediaType.IMAGE_GIF_VALUE;
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "svg":
                return "image/svg+xml";

            // 视频
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "webm":
                return "video/webm";
            case "mkv":
                return "video/x-matroska";

            // 音频
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";

            // 文档
            case "pdf":
                return MediaType.APPLICATION_PDF_VALUE;
            case "txt":
                return MediaType.TEXT_PLAIN_VALUE;
            case "html":
            case "htm":
                return MediaType.TEXT_HTML_VALUE;
            case "xml":
                return MediaType.APPLICATION_XML_VALUE;
            case "json":
                return MediaType.APPLICATION_JSON_VALUE;

            // Office文档
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

}

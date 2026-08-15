package io.github.jiangood.openadmin.util;

import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

public class ContentTypeTool {
    private ContentTypeTool() {
    }


    public static boolean isVideo(String contentType) {
        return CharSequenceUtil.startWith(contentType, "video");
    }

    /**
     * 根据文件扩展名获取Content-Type字符串
     */
    public static String getContentTypeByExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        extension = extension.toLowerCase();

        return switch (extension) {
            // 图片
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";

            // 视频
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "wmv" -> "video/x-ms-wmv";
            case "webm" -> "video/webm";
            case "mkv" -> "video/x-matroska";

            // 音频
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";

            // 文档
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "txt" -> MediaType.TEXT_PLAIN_VALUE;
            case "html", "htm" -> MediaType.TEXT_HTML_VALUE;
            case "xml" -> MediaType.APPLICATION_XML_VALUE;
            case "json" -> MediaType.APPLICATION_JSON_VALUE;

            // Office文档
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

}

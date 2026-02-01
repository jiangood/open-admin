package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.util.URLUtil;
import io.github.jiangood.openadmin.lang.ContentTypeTool;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

@Slf4j
@Controller
public class FilePreviewController {

    private final Set<String> allowedPreviewTypes = Set.of(
            "jpg", "jpeg", "png", "gif", "pdf", "mp4", "avi", "mov"
    );


    @jakarta.annotation.Resource
    SysFileService sysFileService;

    /**
     * 文件预览入口
     */
    @GetMapping({
            "preview/{id}",
            "admin/preview/{id}",

            // 兼容老系统
            "sysFile/preview/{id}",
            "admin/sysFile/preview/{id}",
            "sysFile/preview/{id}.{suffix}"})
    public ResponseEntity<StreamingResponseBody> previewFile(@PathVariable String id,
                                                             HttpServletRequest request,
                                                             Integer w, // 图片宽度
                                                             @PathVariable(required = false) String suffix) {
        SysFile file = sysFileService.findOne(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        String fileExtension = file.getSuffix();
        if (!allowedPreviewTypes.contains(fileExtension)) {
            log.error("后缀不支持预览 {}", fileExtension);
            return ResponseEntity.badRequest().build();
        }
        try {
            InputStream inputStream = sysFileService.getFileStream(file, w);

            boolean video = ContentTypeTool.isVideo(file.getContentType());
            String disposition = "inline; filename=\"" + URLUtil.encode(id + "." + file.getSuffix()) + "\"";
            if (video) {
                String rangeHeader = request.getHeader("Range");
                // 支持视频流传输（兼容HTML5 video标签）
                if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                    log.trace("视频预览范围 {}", rangeHeader);
                    return handlePartialContent(inputStream, file, rangeHeader);
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSize()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(new MyStreamingResponseBody(inputStream));
            }

            // 非视频的文件
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)

                    // 去掉文件大小的响应头，原因：图片尺寸参数存在时（如?w=400）文件大小不准
                    // .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSize()))

                    // 添加缓存头
                    .eTag(id)
                    .lastModified(file.getUpdateTime().getTime())
                    .body(new MyStreamingResponseBody(inputStream));
        } catch (FileNotFoundException fe) {
            log.info("预览文件失败, 文件不存在 id={}, w={}", file.getId(), w);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.info("预览文件失败, id={}, w={}", file.getId(), w);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 处理部分内容请求（如视频播放）
     */
    private ResponseEntity<StreamingResponseBody> handlePartialContent(InputStream inputStream, SysFile file, String rangeHeader) {
        log.trace("处理断点下载");
        long fileSize = file.getSize();
        String[] ranges = rangeHeader.substring(6).split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;

        if (rangeEnd >= fileSize) {
            rangeEnd = fileSize - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(new MyStreamingResponseBody(inputStream, rangeStart, contentLength));
    }

    @ExceptionHandler(Throwable.class)
    public void throwable(Throwable e, HttpServletRequest request) {
        log.error("预览文件时，连接出错 {} {} {}", request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
    }

    private static class MyStreamingResponseBody implements StreamingResponseBody {


        InputStream inputStream;

        long start;
        long contentLength;

        public MyStreamingResponseBody(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public MyStreamingResponseBody(InputStream inputStream, long start, long contentLength) {
            this.inputStream = inputStream;
            this.start = start;
            this.contentLength = contentLength;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            try {
                if (start > 0) {
                    // 跳过起始字节
                    IOUtils.skipFully(inputStream, start);
                    // 复制指定范围的字节
                    IOUtils.copyLarge(inputStream, outputStream, 0, contentLength);
                } else {
                    // 复制指定范围的字节
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            } finally {
                IOUtils.close(inputStream);
            }
        }
    }
}

package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.net.URLEncodeUtil;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import io.github.jiangood.openadmin.util.ContentTypeTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * 文件预览（全局文件访问）
 * <p>
 * 统一走 /file 前缀，C 端可复用；可通过 nginx 将该前缀代理到真实对象存储
 * /file/public/xxx 免登录；/file/private/xxx 需登录（由 SecurityConfig 控制）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FilePreviewController {

    private static final String ACCEPT_RANGES_VAL = "bytes";

    private final SysFileService service;

    private final Set<String> allowedPreviewTypes = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "mp4", "avi", "mov"
    );

    /**
     * 预览文件
     * <p>
     * 支持 ?thumb=1：优先返回缩略图（{objectName} 推导 .thumb），缩略图不存在时回退原图
     */
    @GetMapping("/file/{*objectName}")
    public ResponseEntity<StreamingResponseBody> preview(@PathVariable String objectName,
                                                         @RequestParam(value = "thumb", required = false, defaultValue = "false") boolean thumb,
                                                         HttpServletRequest request) {
        objectName = stripLeadingSlash(objectName);
        SysFile file = service.findByObjectName(objectName);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        String fileExtension = file.getSuffix();
        if (!allowedPreviewTypes.contains(fileExtension)) {
            log.error("后缀不支持预览 {}", fileExtension);
            return ResponseEntity.badRequest().build();
        }

        // 缩略图请求：若缩略图存在则流式返回缩略图，否则回退原图（兼容存量数据）
        String streamObjectName = objectName;
        String eTag = objectName;
        if (thumb) {
            String thumbObjectName = SysFileService.thumbKeyOf(objectName);
            if (service.isPhysicalFileExist(thumbObjectName)) {
                streamObjectName = thumbObjectName;
                eTag = thumbObjectName;
            } else {
                log.trace("缩略图不存在，回退原图 objectName={}", objectName);
            }
        }

        try {
            InputStream inputStream = service.getFileStreamByObjectName(streamObjectName);

            boolean video = ContentTypeTool.isVideo(file.getContentType());
            String disposition = "inline; filename=\"" + URLEncodeUtil.encode(FileNameUtil.mainName(streamObjectName)) + "\"";
            if (video) {
                String rangeHeader = request.getHeader("Range");
                if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                    log.trace("视频预览范围 {}", rangeHeader);
                    return handlePartialContent(inputStream, file, rangeHeader);
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSize()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                        .header(HttpHeaders.ACCEPT_RANGES, ACCEPT_RANGES_VAL)
                        .body(new MyStreamingResponseBody(inputStream));
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .eTag(eTag)
                    .lastModified(file.getUpdateTime() == null ? 0 : file.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .body(new MyStreamingResponseBody(inputStream));
        } catch (FileNotFoundException fe) {
            log.info("预览文件失败, 文件不存在 objectName={}", objectName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("预览文件失败, objectName={}", objectName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<StreamingResponseBody> handlePartialContent(InputStream inputStream, SysFile file, String rangeHeader) {
        log.trace("处理断点下载");
        long fileSize = file.getSize();

        long[] range = parseRange(rangeHeader, fileSize);
        if (range.length == 0) {
            // 非法或暂不支持的 Range（空段/后缀范围之外的多段等），忽略该头返回全量内容
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getSize()))
                    .header(HttpHeaders.ACCEPT_RANGES, ACCEPT_RANGES_VAL)
                    .body(new MyStreamingResponseBody(inputStream));
        }
        long rangeStart = range[0];
        long rangeEnd = range[1];

        long contentLength = rangeEnd - rangeStart + 1;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                .header(HttpHeaders.ACCEPT_RANGES, ACCEPT_RANGES_VAL)
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(new MyStreamingResponseBody(inputStream, rangeStart, contentLength));
    }

    /**
     * 解析单段 Range 头，支持 bytes=start-end / bytes=start- / bytes=-suffix 三种合法格式。
     * 空段、多段、非法数字等不支持的情况返回空数组（调用方回退全量返回）。
     */
    private static long[] parseRange(String rangeHeader, long fileSize) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return new long[0];
        }
        String spec = rangeHeader.substring(6);
        if (spec.isEmpty() || spec.contains(",")) {
            return new long[0];
        }
        String[] ranges = spec.split("-");
        if (ranges.length > 2) {
            return new long[0];
        }
        String startPart = ranges[0];
        String endPart = ranges.length > 1 ? ranges[1] : null;
        if (startPart.isEmpty() && (endPart == null || endPart.isEmpty())) {
            return new long[0];
        }

        long[] range = computeRange(startPart, endPart, fileSize);
        if (range == null) {
            return new long[0];
        }
        long rangeStart = range[0];
        long rangeEnd = range[1];
        if (rangeStart < 0 || rangeStart >= fileSize || rangeStart > rangeEnd) {
            return new long[0];
        }
        if (rangeEnd >= fileSize) {
            rangeEnd = fileSize - 1;
        }
        return new long[]{rangeStart, rangeEnd};
    }

    private static long[] computeRange(String startPart, String endPart, long fileSize) {
        try {
            if (startPart.isEmpty()) {
                // 后缀范围，如 bytes=-500，取文件末尾 500 字节
                long suffixLength = Long.parseLong(endPart);
                if (suffixLength <= 0) {
                    return null;
                }
                return new long[]{Math.max(0, fileSize - suffixLength), fileSize - 1};
            }
            long rangeStart = Long.parseLong(startPart);
            long rangeEnd = endPart == null || endPart.isEmpty() ? fileSize - 1 : Long.parseLong(endPart);
            return new long[]{rangeStart, rangeEnd};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stripLeadingSlash(String objectName) {
        return objectName.startsWith("/") ? objectName.substring(1) : objectName;
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
                    IOUtils.skipFully(inputStream, start);
                    IOUtils.copyLarge(inputStream, outputStream, 0, contentLength);
                } else {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            } finally {
                IOUtils.close(inputStream);
            }
        }
    }
}

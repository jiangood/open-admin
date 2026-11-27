package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.spi.CompletedUploadPart;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.StringReq;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.SysFileConstants;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件
 */
@Slf4j
@RestController
@RequestMapping(SysFileConstants.BASE_PATH)
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService service;


    @HasPermission("sys-file:read")
    @GetMapping("page")
    public AjaxResult page(String dateRange,
                           String originName,
                           String objectName,
                           String folder,
                           FileStatus status,
                           String type,
                           @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysFile> q = Spec.of();
        q.betweenDateRange("createTime", dateRange, true);
        q.like(SysFile.Fields.originName, originName);
        q.like(SysFile.Fields.objectName, objectName);
        q.eq(SysFile.Fields.status, status);
        q.eq(SysFile.Fields.type, type);
        if (CharSequenceUtil.isNotBlank(folder)) {
            // 按左侧文件夹过滤，包含其所有子目录（leftLike 生成 value%）
            q.leftLike(SysFile.Fields.objectName, folder + "/");
        }

        Page<SysFile> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    /**
     * 左侧文件夹树（按 objectName 的存储目录生成）
     */
    @HasPermission("sys-file:read")
    @GetMapping("folder-tree")
    public AjaxResult folderTree() {
        return AjaxResult.ok().data(service.getFolderTree());
    }


    /**
     * 上传文件
     */
    @PostMapping("upload")
    public AjaxResult upload(@RequestPart("file") MultipartFile file,
                             @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic) throws Exception {
        SysFile sysFile = service.uploadFile(file, isPublic);

        // location 供富文本直接使用，返回含 context-path 的相对路径（如 /dev-background/file/xxx.jpg）
        String location = service.getPreviewUrl(sysFile.getObjectName());

        return AjaxResult.ok()
                .putExtData("location", location) // 兼容富文本的格式
                .data("objectName", sysFile.getObjectName())
                .data("name", sysFile.getOriginName());
    }

    /**
     * 上传图片（压缩图 + 缩略图，一次请求两份文件）
     */
    @PostMapping("upload-image")
    public AjaxResult uploadImage(@RequestPart("file") MultipartFile file,
                                  @RequestPart("thumb") MultipartFile thumb,
                                  @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic) throws Exception {
        SysFile sysFile = service.uploadImage(file, thumb, isPublic);

        String location = service.getPreviewUrl(sysFile.getObjectName());
        String thumbObjectName = SysFileService.thumbKeyOf(sysFile.getObjectName());

        return AjaxResult.ok()
                .putExtData("location", location) // 兼容富文本的格式
                .data("objectName", sysFile.getObjectName())
                .data("thumbObjectName", thumbObjectName)
                .data("name", sysFile.getOriginName());
    }

    /**
     * 下载文件
     */
    @GetMapping("download/{*objectName}")
    public void download(@PathVariable String objectName, HttpServletResponse response) throws Exception {
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }
        service.download(objectName, response);
    }


    @GetMapping("detail")
    public AjaxResult detail(String objectName) {
        SysFile sysFile = service.findByObjectName(objectName);
        return AjaxResult.ok().data(sysFile);
    }


    @HasPermission("sys-file:delete")
    @PostMapping("delete")
    public AjaxResult delete(@RequestBody StringReq stringReq) throws Exception {
        service.deleteByObjectName(stringReq.getValue());
        return AjaxResult.ok();
    }

    /**
     * 批量删除文件（按 id）
     */
    @HasPermission("sys-file:delete")
    @PostMapping("delete-batch")
    public AjaxResult deleteBatch(@RequestBody List<String> ids) {
        return AjaxResult.ok().data(service.deleteBatch(ids));
    }

    // ===================== 直传上传（前端直连对象存储，S3 预签名） =====================

    /**
     * 准备直传：生成服务端 objectName 并注册 TEMP SysFile，返回预签名 PUT 或分片上传信息。
     * 存储不支持直传时返回 supported=false，前端应回退 /upload 代理上传。
     */
    @PostMapping("direct-upload/prepare")
    public AjaxResult directUploadPrepare(@RequestBody DirectUploadPrepareReq req) {
        Map<String, Object> info = service.prepareDirectUpload(req.getOriginName(), req.getSize(), req.getIsPublic(), req.getContentType());
        AjaxResult r = AjaxResult.ok().data(info);
        // 兼容富文本等读取 location 的场景
        if (info.get("location") != null) {
            r.putExtData("location", info.get("location"));
        }
        return r;
    }

    /**
     * 获取分片直传某个 part 的预签名 URL。
     */
    @PostMapping("direct-upload/multipart/part-url")
    public AjaxResult directUploadPartUrl(@RequestBody DirectUploadPartUrlReq req) {
        Map<String, Object> info = service.presignDirectUploadPart(req.getObjectName(), req.getUploadId(), req.getPartNumber());
        return AjaxResult.ok().data(info);
    }

    /**
     * 完成分片直传：合并分片并校验对象存在。
     */
    @PostMapping("direct-upload/multipart/complete")
    public AjaxResult directUploadMultipartComplete(@RequestBody DirectUploadMultipartCompleteReq req) {
        Map<String, Object> info = service.completeDirectMultipartUpload(req.getObjectName(), req.getUploadId(), req.getParts());
        AjaxResult r = AjaxResult.ok()
                .data("objectName", info.get("objectName"))
                .data("name", info.get("name"));
        if (info.get("location") != null) {
            r.putExtData("location", info.get("location"));
        }
        return r;
    }

    /**
     * 终止分片直传（取消/失败时清理，幂等）。
     */
    @PostMapping("direct-upload/multipart/abort")
    public AjaxResult directUploadMultipartAbort(@RequestBody DirectUploadAbortReq req) {
        service.abortDirectMultipartUpload(req.getObjectName(), req.getUploadId());
        return AjaxResult.ok();
    }

    /**
     * 完成单文件 PUT 直传：校验对象存在后返回与 /upload 一致的文件信息。
     */
    @PostMapping("direct-upload/complete")
    public AjaxResult directUploadComplete(@RequestBody DirectUploadCompleteReq req) {
        Map<String, Object> info = service.completeDirectUpload(req.getObjectName());
        AjaxResult r = AjaxResult.ok()
                .data("objectName", info.get("objectName"))
                .data("name", info.get("name"));
        if (info.get("location") != null) {
            r.putExtData("location", info.get("location"));
        }
        return r;
    }

    @Data
    public static class DirectUploadPrepareReq {
        private String originName;
        private Long size;
        private Boolean isPublic = true;
        private String contentType;
    }

    @Data
    public static class DirectUploadPartUrlReq {
        private String objectName;
        private String uploadId;
        private int partNumber;
    }

    @Data
    public static class DirectUploadMultipartCompleteReq {
        private String objectName;
        private String uploadId;
        private List<CompletedUploadPart> parts;
    }

    @Data
    public static class DirectUploadAbortReq {
        private String objectName;
        private String uploadId;
    }

    @Data
    public static class DirectUploadCompleteReq {
        private String objectName;
    }
}



package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.StringReq;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.SysFileConstants;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
                           FileStatus status,
                           String type,
                           @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysFile> q = Spec.of();
        q.betweenDateRange("createTime", dateRange, true);
        q.eq(SysFile.Fields.originName, originName);
        q.eq(SysFile.Fields.objectName, objectName);
        q.eq(SysFile.Fields.status, status);
        q.eq(SysFile.Fields.type, type);

        Page<SysFile> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }


    /**
     * 上传文件
     */
    @PostMapping("upload")
    public AjaxResult upload(@RequestPart("file") MultipartFile file,
                             @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic) throws Exception {
        SysFile sysFile = service.uploadFile(file, isPublic);

        // location 供富文本直接使用，返回含 context-path 的相对路径（如 /example/file/xxx.jpg）
        String location = service.getPreviewUrl(sysFile.getObjectName());

        return AjaxResult.ok()
                .putExtData("location", location) // 兼容富文本的格式
                .data("objectName", sysFile.getObjectName())
                .data("name", sysFile.getOriginName());
    }

    /**
     * 上传图片（压缩图 + 缩略图，一次请求两份文件）
     */
    @PostMapping("uploadImage")
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
    @PostMapping("deleteBatch")
    public AjaxResult deleteBatch(@RequestBody List<String> ids) throws Exception {
        return AjaxResult.ok().data(service.deleteBatch(ids));
    }
}



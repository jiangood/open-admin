package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.StringReq;
import io.github.jiangood.openadmin.framework.enums.FileVisibility;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
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
    @RequestMapping("page")
    public AjaxResult page(String dateRange,
                           String originName,
                           String objectName,
                           MaterialType type,
                           @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysFile> q = Spec.of();
        q.betweenDateRange("createTime", dateRange, true);
        q.eq(SysFile.Fields.originName, originName);
        q.eq(SysFile.Fields.objectName, objectName);
        q.eq(SysFile.Fields.type, type);

        Page<SysFile> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }


    /**
     * 上传文件
     */
    @PostMapping("upload")
    public AjaxResult upload(@RequestPart("file") MultipartFile file,
                             @RequestParam(value = "visibility", required = false) String visibility) throws Exception {
        SysFile sysFile = service.uploadFile(file, FileVisibility.parse(visibility));

        // location 供富文本直接使用，返回含 context-path 的相对路径（如 /example/file/xxx.jpg）
        String location = service.getPreviewUrl(sysFile.getObjectName());

        return AjaxResult.ok()
                .putExtData("location", location) // 兼容富文本的格式
                .data("objectName", sysFile.getObjectName())
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
}



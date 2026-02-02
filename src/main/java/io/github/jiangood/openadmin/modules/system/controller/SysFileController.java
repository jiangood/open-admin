package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.enums.MaterialType;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件
 */
@Slf4j
@RestController
@RequestMapping("admin/sysFile")
public class SysFileController {

    @Resource
    private SysFileService service;


    @PreAuthorize("hasAuthority('sysFile:view')")
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
    public AjaxResult upload(@RequestPart("file") MultipartFile file) throws Exception {
        SysFile sysFile = service.uploadFile(file);

        String location = service.getPreviewUrl(sysFile.getId());

        return AjaxResult.ok()
                .putExtData("location", location)    // 兼容 tiny mce
                .data("id", sysFile.getId())
                .data("name", sysFile.getOriginName());
    }

    /**
     * 下载文件
     */
    @GetMapping("download")
    public void download(String id, HttpServletResponse response) throws Exception {
        service.download(id, response);
    }

    @GetMapping("download/{fileId}")
    public void downloadFile(@PathVariable String fileId, HttpServletResponse response) throws Exception {
        service.download(fileId, response);
    }


    @GetMapping("detail")
    public AjaxResult detail(String id) {
        return AjaxResult.ok().data(service.findOne(id));
    }


    @PreAuthorize("hasAuthority('sysFile:delete')")
    @RequestMapping("delete")
    public AjaxResult delete(String id) throws Exception {
        service.deleteById(id);
        return AjaxResult.ok();
    }

}

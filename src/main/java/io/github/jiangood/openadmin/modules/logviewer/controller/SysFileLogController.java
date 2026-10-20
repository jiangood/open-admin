package io.github.jiangood.openadmin.modules.logviewer.controller;

import io.github.jiangood.openadmin.modules.logviewer.dto.LogChunk;
import io.github.jiangood.openadmin.modules.logviewer.perm.LogKeyPermissionRegistry;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 将日志文件显示出来
 * <p>
 * 查看权限按 key 前缀由各业务模块注册（见 {@link LogKeyPermissionRegistry}），
 * 本模块不绑定具体业务权限。
 */
@RestController
@RequestMapping("admin/sys/log")
public class SysFileLogController {

    @Resource
    private FileLogService fileLogService;

    @Resource
    private LogKeyPermissionRegistry logKeyPermissionRegistry;

    /**
     * 增量读取日志（默认 JSON）。
     * <p>
     * 前端日志弹窗按返回的 {@code nextOffset} 反复调用即可只取新增内容；
     * 不传 offset 时从头读，行为等价于一次全量读取。
     *
     * @param key    日志 key，如 {@code job/{logId}}
     * @param offset 起始字节偏移，默认 0
     * @param length 本次最多读取的字节数，默认 256KB
     */
    @GetMapping("{*key}")
    public AjaxResult log(@PathVariable String key,
                          @RequestParam(defaultValue = "0") long offset,
                          @RequestParam(defaultValue = "0") int length) throws IOException {
        logKeyPermissionRegistry.checkPermission(key);
        LogChunk chunk = fileLogService.readChunk(key, offset, length);
        return AjaxResult.ok().data(chunk);
    }

    /**
     * 以纯文本返回完整日志，供浏览器新窗口打开 / 下载
     */
    @GetMapping("raw/{*key}")
    public void raw(@PathVariable String key, HttpServletResponse response) throws IOException {
        logKeyPermissionRegistry.checkPermission(key);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain; charset=utf-8");

        PrintWriter out = response.getWriter();
        out.print(fileLogService.readLogContent(key));
    }
}

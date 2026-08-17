package io.github.jiangood.openadmin.modules.logviewer.controller;

import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 将日志文件显示出来
 */
@RestController
@RequestMapping("admin/sys/log")
public class SysFileLogController {

    @Resource
    private FileLogService fileLogService;

    @GetMapping("{*key}")
    public void log(@PathVariable String key, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain; utf-8");

        PrintWriter out = response.getWriter();
        out.print(fileLogService.readLogContent(key));
    }
}

package io.github.jiangood.openadmin.modules.log.file;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

/**
 * 将日志文件显示出来
 */
@RestController
@RequestMapping("admin/sys/log")
public class SysFileLogController {


    @Resource
    FileLogConfig cfg;


    @GetMapping("{key}")
    public void log(@PathVariable String key, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain; utf-8");

        PrintWriter out = response.getWriter();

        File file = cfg.buildLogFile(key);


        if (!file.exists()) {
            out.println("文件不存在:" + file.getAbsolutePath());
            return;
        }

        FileInputStream is = new FileInputStream(file);
        IOUtils.copy(is, out, "utf-8");
        is.close();
    }


}

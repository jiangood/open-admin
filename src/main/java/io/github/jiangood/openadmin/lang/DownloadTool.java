package io.github.jiangood.openadmin.lang;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件下载工具类
 */
@Slf4j
public class DownloadTool {


    public static void download(String fileName, byte[] fileBytes, HttpServletResponse response) {
        try {
            setDownloadParam(fileName, fileBytes.length, response);
            IoUtil.write(response.getOutputStream(), true, fileBytes);
        } catch (IOException e) {
            log.error(">>> 下载文件异常，具体信息为：{}", e.getMessage());
            throw new IllegalStateException("下载文件错误");
        }
    }

    public static void download(String fileName, InputStream inputStream, long fileSize, HttpServletResponse response) {
        try (InputStream in = inputStream) {
            setDownloadParam(fileName, fileSize, response);
            IOUtils.copy(in, response.getOutputStream());
        } catch (IOException e) {
            log.error(">>> 下载文件异常，具体信息为：{}", e.getMessage());
            throw new IllegalStateException("下载文件错误");
        }
    }


    /**
     * 下载文件
     *
     * @param file     要下载的文件
     * @param response 响应
     */
    public static void download(File file, HttpServletResponse response) {
        String fileName = file.getName();
        download(fileName, FileUtil.getInputStream(file), FileUtil.size(file), response);
    }

    /**
     * @param fileName
     * @param contentLength 不知道的情况下填0或-1
     * @param response
     */
    public static void setDownloadParam(String fileName, long contentLength, HttpServletResponse response) {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLUtil.encode(fileName) + "\"");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/octet-stream;charset=UTF-8");
        if (contentLength > 0) {
            response.addHeader("Content-Length", String.valueOf(contentLength));
        }
    }

}

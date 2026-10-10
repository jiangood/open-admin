package io.github.jiangood.openadmin.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.net.URLEncodeUtil;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseTool {
    private ResponseTool() {
    }


    public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel;charset=utf-8";
    public static final String CONTENT_TYPE_PDF = "application/pdf";

    public static final String CONTENT_TYPE_STREAM = "application/octet-stream";

    public static void setDownloadHeader(String filename, String contentType, HttpServletResponse response) {
        filename = URLEncodeUtil.encode(filename, StandardCharsets.UTF_8);

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setHeader("Access-Control-Expose-Headers", "content-disposition");
    }

    public static void setDownloadExcelHeader(String filename, HttpServletResponse response) {
        filename = URLEncodeUtil.encode(filename, StandardCharsets.UTF_8);
        response.setContentType(CONTENT_TYPE_EXCEL);
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setHeader("Access-Control-Expose-Headers", "content-disposition");
    }


    public static void setCrossDomain(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,jwt,Authorization");

        response.addHeader("Access-Control-Max-Age", "3600");
    }


    public static void responseExceptionError(HttpServletResponse response,
                                              Integer code,
                                              String message) {
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        response.setContentType("application/json;charset=utf-8");
        AjaxResult result = AjaxResult.err().code(code).msg(message);
        String errorResponseJsonData = JsonTool.toPrettyJsonQuietly(result);
        try {
            response.setStatus(code);
            response.getWriter().write(errorResponseJsonData);
        } catch (Exception e) {
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    public static void responseJson(HttpServletResponse response, Object data) {
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        response.setContentType("application/json;charset=utf-8");
        String errorResponseJsonData = JsonTool.toPrettyJsonQuietly(data);
        try {
            if (errorResponseJsonData != null) {
                response.getWriter().write(errorResponseJsonData);
            } else {
                response.getWriter().write("null");
            }
        } catch (Exception e) {
            log.error(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    public static void response(HttpServletResponse response, AjaxResult result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonTool.toJson(result));
        response.getWriter().flush();
    }


    public static void responseHtml(HttpServletResponse response, String html) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().close();
    }
}

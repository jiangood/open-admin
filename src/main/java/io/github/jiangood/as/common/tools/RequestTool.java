package io.github.jiangood.as.common.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestTool {

    /**
     * 获取基础URL， 如http://127.0.0.1
     * 注意：末尾不含斜杠
     *
     * @param request 请求
     * @return 基础URL
     */
    public static String getBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return "/";
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        // 判断请求头是否指定了协议
        String xForwardedProto = request.getHeader("x-forwarded-proto");
        if (StrUtil.isNotEmpty(xForwardedProto)) {
            scheme = xForwardedProto;
        }


        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(host);

        if (port != 80 && port != 443) {
            sb.append(":").append(port);
        }


        return sb.toString();
    }

    public static Map<String, String> getParamMap(HttpServletRequest request) {
        Enumeration<String> names = request.getParameterNames();

        Map<String, String> initData = new HashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();

            if (!initData.containsKey(name)) {
                initData.put(name, request.getParameter(name));
            }
        }

        return initData;
    }

    public static HttpServletRequest currentRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra != null) {
            return sra.getRequest();
        }
        return null;
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }


}

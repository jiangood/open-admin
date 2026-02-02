package io.github.jiangood.openadmin.lang;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * HttpServlet工具类，获取当前request和response
 */
public class HttpServletTool {

    /**
     * 获取当前请求的request对象
     *
     * @return request对象
     * @throws IllegalStateException 当请求上下文不存在时抛出
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("Request context not found");
        }

        return requestAttributes.getRequest();
    }

    /**
     * 获取当前请求的response对象
     *
     * @return response对象
     * @throws IllegalStateException 当请求上下文不存在时抛出
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("Request context not found");
        }

        return requestAttributes.getResponse();
    }
}

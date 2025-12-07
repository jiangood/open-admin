package io.admin.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/***
 * 缓存json请求体
 * 方便后续再取出来
 *
 * 初因未为了 RequestBodyKeysArgumentResolver
 *
 */
@Component
public class CachingJsonRequestBodyFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        String contentType = request.getContentType();

        boolean isJson = contentType != null && contentType.toLowerCase().contains("json");
        boolean isPost = "POST".equalsIgnoreCase(method);
        boolean isMe = request instanceof ContentCachingRequestWrapper;

        if (isJson && isPost && !isMe) {
            request = new ContentCachingRequestWrapper(request);
        }

        filterChain.doFilter(request, response);
    }
}

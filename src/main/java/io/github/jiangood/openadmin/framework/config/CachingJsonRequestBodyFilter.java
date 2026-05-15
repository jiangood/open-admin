package io.github.jiangood.openadmin.framework.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

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
            request = new ContentCachingRequestWrapper(request, -1);
        }

        filterChain.doFilter(request, response);
    }
}

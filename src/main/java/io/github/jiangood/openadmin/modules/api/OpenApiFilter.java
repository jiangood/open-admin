package io.github.jiangood.openadmin.modules.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.modules.api.ApiConstant;
import io.github.jiangood.openadmin.modules.api.ApiResult;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.service.ApiAccessLogService;
import io.github.jiangood.openadmin.modules.api.service.ApiAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class OpenApiFilter extends OncePerRequestFilter {

    private final ApiAccountService apiAccountService;
    private final ApiAccessLogService apiAccessLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith(ApiConstant.BASE_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            // 1. 从 Header 提取 Bearer Token
            String header = request.getHeader("Authorization");
            boolean headerOK = header != null && header.startsWith("Bearer ");
            if (!headerOK) {
                ResponseTool.responseJson(response, ApiResult.error(2001, "请输入有效的 Token"));
                return;
            }
            String token = header.substring(7);
            ApiAccount account = apiAccountService.findByToken(token);
            if (account == null || !account.getEnable()) {
                ResponseTool.responseJson(response, ApiResult.error(3001, "账号不存在或已禁用"));
                return;
            }
            if (account.getEndTime() != null && DateUtil.current() > account.getEndTime().getTime()) {
                ResponseTool.responseJson(response, ApiResult.error(3002, "账号已过期"));
                return;
            }
            String ip = JakartaServletUtil.getClientIP(request);
            if (StrUtil.isNotEmpty(account.getAccessIp()) && !account.getAccessIp().contains(ip)) {
                ResponseTool.responseJson(response, ApiResult.error(3003, "IP白名单限制," + ip));
                return;
            }

            // 构建 Spring Security 的认证对象


            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            for (String perm : account.getPerms()) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("username", null, authorities);



            // 存入上下文，后续 Controller 就可以直接获取用户信息
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);


            // 保存日志
            long time = System.currentTimeMillis() - startTime;
            apiAccessLogService.add(System.currentTimeMillis(), account, uri, ip, time);


        } catch (Exception e) {
            ResponseTool.responseJson(response, ApiResult.error(5000, "系统错误," + e.getMessage()));
        }
    }


}
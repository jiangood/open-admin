package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.util.ResponseTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

/**
 * 会话被并发登录顶下线时的响应策略。
 * <p>
 * Spring Security 默认策略（{@code ConcurrentSessionFilter$ResponseBodySessionInformationExpiredStrategy}）
 * 不设置状态码，直接以 HTTP 200 + 纯文本返回，前端 {@code HttpClient} 无法识别为登录失效，导致静默失败。
 * 这里改为与框架其它 401 一致：HTTP 401 + {@link AjaxResult} JSON，
 * 前端据此广播 {@code loginExpired} 并提示具体原因。
 */
@RequiredArgsConstructor
public class ConcurrentSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    /**
     * 响应体扩展字段 loginExpiredReason 的取值：并发登录被顶下线
     */
    public static final String REASON_CONCURRENT_LOGIN = "CONCURRENT_LOGIN";

    /**
     * 响应体扩展字段名，前端据此区分「并发被顶」与「空闲超时」
     */
    public static final String EXT_KEY_REASON = "loginExpiredReason";

    private final SystemProperties systemProperties;

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletResponse response = event.getResponse();

        // ResponseTool 只写 body 与 Content-Type，状态码必须在此显式设置
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        AjaxResult result = AjaxResult.err(HttpStatus.UNAUTHORIZED.value(), systemProperties.getConcurrentLoginMessage())
                .putExtData(EXT_KEY_REASON, REASON_CONCURRENT_LOGIN);
        ResponseTool.response(response, result);
    }
}

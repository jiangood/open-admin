package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static io.github.jiangood.openadmin.framework.MessageConst.MGS_FORBIDDEN;
import static io.github.jiangood.openadmin.framework.MessageConst.MSG_UNAUTHORIZED;


@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final SystemProperties systemProperties;

    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException ex) {
        if (systemProperties.isPrintGlobalException()) {
            log.error(MGS_FORBIDDEN, ex);
        }
        String msg = ex.getMessage();
        if (msg.startsWith(MGS_FORBIDDEN)) {
            return AjaxResult.err(HttpStatus.FORBIDDEN.value(), msg);
        }
        return AjaxResult.FORBIDDEN;
    }

    @ExceptionHandler(AuthenticationException.class)
    public AjaxResult handleAuthenticationException(AuthenticationException ex) {
        if (systemProperties.isPrintGlobalException()) {
            log.error(MSG_UNAUTHORIZED, ex);
        }
        return AjaxResult.UNAUTHORIZED;
    }


}



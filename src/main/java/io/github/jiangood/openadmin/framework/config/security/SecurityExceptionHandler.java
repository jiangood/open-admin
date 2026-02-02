package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static io.github.jiangood.openadmin.lang.dto.CommonMessage.FORBIDDEN_MESSAGE;


@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException ex) {
        String msg = ex.getMessage();
        if (msg.startsWith(FORBIDDEN_MESSAGE)) {
            return AjaxResult.err(HttpStatus.FORBIDDEN.value(), msg);
        }
        return AjaxResult.FORBIDDEN;
    }

    @ExceptionHandler(AuthenticationException.class)
    public AjaxResult handleAuthenticationException(AuthenticationException ex) {
        return AjaxResult.UNAUTHORIZED;
    }


}



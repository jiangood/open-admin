package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.ExceptionToMessageTool;
import io.github.jiangood.openadmin.util.HttpServletTool;
import io.github.jiangood.openadmin.util.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import static io.github.jiangood.openadmin.framework.MessageConst.MGS_FORBIDDEN;
import static io.github.jiangood.openadmin.framework.MessageConst.MSG_UNAUTHORIZED;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {



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

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(Throwable.class)
    public AjaxResult throwable(Throwable e, HttpServletRequest request) {
        log.error(">>> 服务器运行异常 ", e);
        log.info("请求地址 {}", request.getRequestURI());
        if (!systemProperties.isPrintGlobalException()) {
            return AjaxResult.err().msg("服务器忙，请稍后重试");
        }
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public AjaxResult noResourceFoundException(NoResourceFoundException e) {
        return AjaxResult.err().code(404).msg("接口或资源不存在 " + e.getMessage());
    }

    /**
     * 请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public AjaxResult missParamException(MissingServletRequestParameterException e) {
        log.warn("请求参数缺失：{}", e.getMessage());
        String parameterName = e.getParameterName();
        String message = CharSequenceUtil.format("缺少请求的参数{}", parameterName);
        return AjaxResult.err().code(500).msg(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("请求参数未通过校验：{}", e.getMessage());

        StringBuilder sb = new StringBuilder();
        for (ObjectError error : e.getAllErrors()) {
            sb.append(error.getDefaultMessage()).append(" ");
        }
        return AjaxResult.err().code(500).msg(sb.toString());
    }


    /**
     * 拦截资源找不到的运行时异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AjaxResult notFound(NoHandlerFoundException e) {
        log.error(">>> 资源不存在异常，具体信息为：{}", e.getMessage() + "，请求地址为:" + HttpServletTool.getRequest().getRequestURI());
        return AjaxResult.err().code(404).msg("资源路径不存在，请检查请求地址，请求地址为:" + HttpServletTool.getRequest().getRequestURI());
    }


    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AjaxResult fileNotFoundException(FileNotFoundException e) {
        String uri = HttpServletTool.getRequest().getRequestURI();
        log.error("文件不存在：{} ,请求地址为 {}", e.getMessage(), uri);
        return AjaxResult.err().code(404).msg(e.getMessage()).data("请求路径：" + uri);
    }


    /**
     * 拦截权限异常
     */
    @ExceptionHandler(BusinessException.class)
    public AjaxResult systemException(BusinessException e) {
        return AjaxResult.err(e.getCode(), e.getMessage());
    }


    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public AjaxResult handleAssertionError(RuntimeException e) {
        log.error(">>> 运行时异常，具体信息为：{}", e.getMessage());
        if (systemProperties.isPrintGlobalException()) {
            log.error("打印异常已开启,以下是异常详细信息", e);
        }
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public AjaxResult constraintViolationException(ConstraintViolationException e) {
        log.warn("约束异常:{}", e.getMessage());
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public AjaxResult dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据处理异常", e);
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }


    @ExceptionHandler(TransactionSystemException.class)
    public AjaxResult transactionSystemException(TransactionSystemException e) {
        log.error("事务异常", e);
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public AjaxResult invalidDataAccessApiUsageException(InvalidDataAccessApiUsageException e, HttpServletRequest request) {
        log.error("数据访问API使用异常", e);
        Throwable throwable = e.getCause();
        return AjaxResult.err().msg(throwable.getMessage());
    }


    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(SQLException.class)
    public AjaxResult sqlException(SQLException e) {
        log.error("SQL异常", e);
        return AjaxResult.err().msg(ExceptionToMessageTool.convert(e));
    }


    // io中断，如预览视频
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    @ResponseBody
    public ResponseEntity<Void> asyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        // 客户端断开连接是正常情况，返回204无内容
        return ResponseEntity.noContent().build();
    }


    /**
     * 上传文件超过大小限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public AjaxResult maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制: {}", e.getMessage());
        return AjaxResult.err().msg("上传文件过大，请压缩后再试");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求内容错误", e);
        return AjaxResult.err().msg("请求内容不可读");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult methodNotSupported(HttpRequestMethodNotSupportedException e) {
        return AjaxResult.err().msg("不支持请求方法" + e.getMethod());
    }


}



package io.github.jiangood.openadmin.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.annotation.RemarkTool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

import java.lang.reflect.Field;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;


/**
 * 将异常转换为友好的消息
 */
@Slf4j
public class ExceptionToMessageTool {
    private ExceptionToMessageTool() {
    }


    public static String convert(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = dispatch(throwable);

        // 中文则提示中文，非中文则使用默认提示
        if (!StringTool.hasChinese(message)) {
            message = "服务器忙";
        }
        return message;
    }

    private static String dispatch(Throwable throwable) {
        if (throwable instanceof ConstraintViolationException e) {
            return convert(e);
        }


        if (throwable instanceof DataIntegrityViolationException e) {
            return convert(e);
        }
        if (throwable instanceof TransactionSystemException e) {
            return convert(e);
        }


        return throwable.getMessage();
    }

    private static String convert(DataIntegrityViolationException e) {
        if (e.getCause() != null && e.getCause().getCause() != null) {
            Throwable ex = e.getCause().getCause();
            String msg = ex.getMessage();

            if (msg.contains("Data too long")) {
                return "数据长度超过限制，请修改！";
            }


            if (ex instanceof SQLIntegrityConstraintViolationException) {
                if (msg.startsWith("Duplicate")) {
                    Matcher m = Pattern.compile("'(.*?)'").matcher(msg);
                    if (m.find() && CharSequenceUtil.isNotBlank(m.group(1))) {
                        return "操作失败，数据重复：" + m.group(1);
                    }
                }

                {
                    // Column 'file_id' cannot be null
                    Matcher m = Pattern.compile("Column '(.*)' cannot be null").matcher(msg);
                    if (m.find() && CharSequenceUtil.isNotEmpty(m.group(1))) {
                        return "字段" + m.group(1) + "不能为空";
                    }
                }

            }
        }
        return "数据已被引用，请检查";
    }

    private static String convert(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        StringBuilder sb = new StringBuilder();

        for (ConstraintViolation<?> v : constraintViolations) {
            String fieldName = v.getPropertyPath().toString();
            String message = v.getMessage();

            Class<?> cls = v.getRootBeanClass();

            Field field = ReflectUtil.getField(cls, fieldName);
            String fieldCnName = RemarkTool.getRemark(field);
            if (fieldCnName != null) {
                fieldName = fieldCnName;
            }

            sb.append("【").append(fieldName).append("】错误：");
            sb.append(message);
            sb.append(" \r\n");
        }

        return sb.toString();
    }


    private static String convert(TransactionSystemException e) {
        if (e.getCause() != null) {
            RollbackException cause = (RollbackException) e.getCause();
            if (cause != null) {
                Throwable cause2 = cause.getCause();
                if (cause2 instanceof ConstraintViolationException) {
                    return convert((ConstraintViolationException) cause2);
                }
            }
        }
        return "服务器忙";
    }
}



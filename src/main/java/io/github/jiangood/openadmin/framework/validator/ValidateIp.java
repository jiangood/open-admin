package io.github.jiangood.openadmin.framework.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * IP 效验注解
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateIp.IpValidator.class)
public @interface ValidateIp {

    String message() default "IP地址不正确";

    class IpValidator implements ConstraintValidator<ValidateIp, String> {


        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value != null && !value.isEmpty()) {
                // 定义正则表达式
                String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
                        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
                // 判断ip地址是否与正则表达式匹配
                // 返回判断信息
                // 返回判断信息
                return value.matches(regex);
            }
            return false;
        }
    }
}

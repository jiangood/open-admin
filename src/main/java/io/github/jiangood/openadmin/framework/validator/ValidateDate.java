package io.github.jiangood.openadmin.framework.validator;

import cn.hutool.core.date.DateUtil;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验日期格式 yyyy-MM-dd
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateDate.MyValidator.class)
public @interface ValidateDate {

    String message() default "日期格式错误，正确格式应为yyyy-MM-dd,如：2022-03-15";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateDate, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                try {
                    DateUtil.parse(str, "yyyy-MM-dd");
                } catch (Exception e) {
                    return false;
                }

            }
            return true;
        }


    }
}

package io.github.jiangood.openadmin.framework.validator;

import cn.hutool.core.lang.Validator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 邮政编码（中国）
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateZipCode.MyValidator.class)
public @interface ValidateZipCode {

    String message() default "邮政编码错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateZipCode, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                return Validator.isZipCode(str);
            }
            return true;
        }


    }
}

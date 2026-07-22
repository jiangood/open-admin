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
 * 统一社会信用代码
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateCreditCode.MyValidator.class)
public @interface ValidateCreditCode {

    String message() default "统一社会信用代码错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateCreditCode, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                return Validator.isCreditCode(str);
            }
            return true;
        }


    }
}

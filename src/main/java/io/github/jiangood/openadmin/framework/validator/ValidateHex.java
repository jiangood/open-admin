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
 * 16进制字符串
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateHex.MyValidator.class)
public @interface ValidateHex {

    String message() default "必须是16进制字符串";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateHex, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                return Validator.isHex(str);
            }
            return true;
        }


    }
}

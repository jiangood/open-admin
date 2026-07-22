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
 * 英文字母 、数字和下划线
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateGeneral.MyValidator.class)
public @interface ValidateGeneral {

    String message() default "只能是英文字母 、数字和下划线";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateGeneral, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                return Validator.isGeneral(str);
            }
            return true;
        }


    }
}

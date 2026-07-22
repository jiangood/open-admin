package io.github.jiangood.openadmin.framework.validator;


import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字母开头
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateStartWithLetter.MyValidator.class)
public @interface ValidateStartWithLetter {

    String message() default "必须以字母开头";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class MyValidator implements ConstraintValidator<ValidateStartWithLetter, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                char c = str.charAt(0);
                return Character.isLetter(c);
            }
            return true;
        }
    }
}

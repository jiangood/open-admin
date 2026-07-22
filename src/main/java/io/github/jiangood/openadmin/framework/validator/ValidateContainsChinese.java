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
 * 包含中文字符
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateContainsChinese.MyChineseValidator.class)
public @interface ValidateContainsChinese {

    String message() default "必须包含中文字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyChineseValidator implements ConstraintValidator<ValidateContainsChinese, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                for (char c : str.toCharArray()) {
                    if (isHan(c)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }

        private boolean isHan(char c) {
            Character.UnicodeScript sc = Character.UnicodeScript.of(c);
            return sc == Character.UnicodeScript.HAN;
        }
    }
}

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
 * 不包含汉字
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateNotContainsChinese.MyChineseValidator.class)
public @interface ValidateNotContainsChinese {

    String message() default "不能包含中文字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyChineseValidator implements ConstraintValidator<ValidateNotContainsChinese, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                for (char c : str.toCharArray()) {
                    if (isHan(c)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isHan(char c) {
            Character.UnicodeScript sc = Character.UnicodeScript.of(c);
            return sc == Character.UnicodeScript.HAN;
        }
    }
}

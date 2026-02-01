package io.github.jiangood.openadmin.framework.validator;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.PasswdStrength;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 密码
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidatePassword.MyValidator.class)
public @interface ValidatePassword {

    String message() default "密码太弱";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidatePassword, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                PasswdStrength.PASSWD_LEVEL level = PasswdStrength.getLevel(str);
                switch (level) {
                    case EASY:
                        return false;

                }
                return Validator.isCarDrivingLicence(str);
            }
            return true;
        }


    }
}

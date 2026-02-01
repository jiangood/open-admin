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
 * IP地址(v4)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateIpv4.MyValidator.class)
public @interface ValidateIpv4 {

    String message() default "IP地址(v4)格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class MyValidator implements ConstraintValidator<ValidateIpv4, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                if (str.contains(",")) {
                    String[] arr = str.split(",");
                    for (String s : arr) {
                        boolean ipv4 = Validator.isIpv4(str);
                        if (!ipv4) {
                            return false;
                        }
                    }
                }
                return Validator.isIpv4(str);
            }
            return true;
        }


    }
}

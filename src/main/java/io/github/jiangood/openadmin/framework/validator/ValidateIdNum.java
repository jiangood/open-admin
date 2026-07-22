package io.github.jiangood.openadmin.framework.validator;

import cn.hutool.core.util.IdcardUtil;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 身份证
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateIdNum.IdNumValidator.class)
public @interface ValidateIdNum {

    String message() default "身份证号码错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class IdNumValidator implements ConstraintValidator<ValidateIdNum, String> {


        @Override
        public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
            if (str != null && !str.isEmpty()) {
                return IdcardUtil.isValidCard(str);
            }
            return true;
        }


    }
}

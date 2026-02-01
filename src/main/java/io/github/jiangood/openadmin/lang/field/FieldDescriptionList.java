package io.github.jiangood.openadmin.lang.field;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FieldDescriptionList {

    FieldDescription[] value() default {};

}

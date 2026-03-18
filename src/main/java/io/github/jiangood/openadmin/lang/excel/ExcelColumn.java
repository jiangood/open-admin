package io.github.jiangood.openadmin.lang.excel;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelColumn {

    /**
     * 表头
     * @return
     */
    String value();

    int seq() default Integer.MAX_VALUE;
}

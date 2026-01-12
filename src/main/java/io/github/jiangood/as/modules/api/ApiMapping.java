package io.github.jiangood.as.modules.api;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ApiMapping {

    /**
     * Api名称
     */
    String name();

    /**
     * Api资源定位
     */
    String action();


    /**
     * Api描述
     */
    String desc() default "";


}

package io.github.jiangood.openadmin.framework.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在枚举常量上，声明该字典项的文本（label）与可选颜色（color）。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictItem {
    String label();

    /**
     * 可选颜色。支持两种格式：
     * <ul>
     *   <li>预设色名：DEFAULT、PROCESSING、SUCCESS、ERROR、WARNING、RED、BLUE、GREEN、GRAY</li>
     *   <li>十六进制：#rgb 或 #rrggbb，如 #ff0000</li>
     * </ul>
     * 留空表示无颜色。
     */
    String color() default "";
}

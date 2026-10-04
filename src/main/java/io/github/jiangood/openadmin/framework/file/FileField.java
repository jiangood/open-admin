package io.github.jiangood.openadmin.framework.file;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注实体上引用框架文件（objectName）的字段。
 * <p>
 * 配合 {@code SysFileService.claim/unclaim(Persistable)} 使用：认领/取消认领时框架自动扫描该注解字段，
 * joinTable 取实体 {@code @Table(name)}，joinId 取 {@code Persistable.getId()}，业务方无需指定字段与表名。
 * <p>
 * 实体无需继承 {@code BaseEntity}，实现 {@code Persistable<String>} 即可。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileField {

    /**
     * true 表示富文本 HTML 字段（自动提取其中引用的全部框架文件 URL）
     */
    boolean html() default false;
}

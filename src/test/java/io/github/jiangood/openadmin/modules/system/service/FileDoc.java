package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.file.FileField;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

/**
 * 不继承 BaseEntity、仅实现 Persistable 的测试实体，
 * 验证文件认领/取消认领的 joinTable/joinId 推导不依赖框架基类
 */
@Getter
@Setter
@Table(name = "test_doc")
public class FileDoc implements Persistable<String> {

    @Id
    private String id;

    @FileField
    private String cover;

    @FileField(html = true)
    private String content;

    @Transient
    @Override
    public boolean isNew() {
        return id == null;
    }
}

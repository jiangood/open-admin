package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("文章")
@Getter
@Setter
@Entity
@Table(name = "sys_article")
@FieldNameConstants
public class Article extends BaseEntity {

    @Column(unique = true, length = 32, nullable = false)
    private String code;

    @Column(length = 200, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20, nullable = false)
    private String position;

    private Integer seq;

    private Boolean enabled;

    @PrePersist
    public void prePersist() {
        if (position == null) {
            position = "none";
        }
        if (seq == null) {
            seq = 0;
        }
        if (enabled == null) {
            enabled = true;
        }
    }
}

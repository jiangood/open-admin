package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
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

    @Column(name = "main_image", length = 200)
    private String mainImage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ArticlePosition position;

    private Integer seq;

    private Boolean enabled;

    /**
     * 发布人姓名（查询时由 createUser 关联 SysUser 填充，不落库）
     */
    @Transient
    private String createUserLabel;

    @PrePersist
    public void prePersist() {
        if (position == null) {
            position = ArticlePosition.NONE;
        }
        if (seq == null) {
            seq = 0;
        }
        if (enabled == null) {
            enabled = true;
        }
    }
}

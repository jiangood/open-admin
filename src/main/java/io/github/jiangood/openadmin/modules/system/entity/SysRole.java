package io.github.jiangood.openadmin.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.util.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Remark("系统角色")
@Entity
@Table(name = "sys_role")
@Getter
@Setter
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role",
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false),
            joinColumns = @JoinColumn(name = "role_id", nullable = false))
    Set<SysUser> users = new HashSet<>();
    @Remark("名称")
    @Column(length = 50, unique = true)
    private String name;
    @Remark("编码")
    @Column(unique = true, length = 20)
    private String code;
    @Remark("排序")
    private Integer seq;
    @Remark("备注")
    private String remark;
    @Remark("启用")
    @Column(nullable = false)
    private Boolean enabled;

    @Remark("权限码")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    private List<String> perms;

    @Remark("菜单")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    private List<String> menus;


    public SysRole(String id) {
        this.setId(id);
    }

    public SysRole() {

    }

    @Transient
    public boolean isAdmin() {
        return "admin".equals(this.code);
    }

    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
    }
}

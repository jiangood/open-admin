package io.admin.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.converter.ToListConverter;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Transient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;



@Remark("系统角色")
@Entity
@Getter
@Setter
@FieldNameConstants
public class SysRole extends BaseEntity {

    /**
     * 是否内置，内置的不让修改
     */
    @Remark("是否内置")
    @Column(nullable = false)
    Boolean builtin;
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
    @Convert(converter = ToListConverter.class)
    @Lob
    private List<String> perms;
    @Remark("菜单")
    @Convert(converter = ToListConverter.class)
    @Lob
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

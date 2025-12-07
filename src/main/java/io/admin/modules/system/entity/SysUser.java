package io.admin.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@FieldNameConstants
@Remark("系统用户")
public class SysUser extends BaseEntity {

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false))
    Set<SysRole> roles = new HashSet<>();
    // 数据权限类型
    @Enumerated(EnumType.STRING)
    DataPermType dataPermType;
    @JsonIgnore
    @Lazy
    @ManyToMany
    @JoinTable(name = "sys_user_data_perm", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "org_id"))
    List<SysOrg> dataPerms = new ArrayList<>();
    /**
     * 所属机构 (公司，单位级别）
     */
    @Remark("所属机构")
    private String unitId;
    @Remark("所属部门")
    private String deptId;
    @Remark("账号")
    @NotNull(message = "账号不能为空")
    @Column(unique = true, length = 50)
    private String account;
    /**
     * 密码， 转换json时不显示，但可接受前端设置
     */
    @Remark("密码")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Remark("姓名")
    @Column(length = 50)
    private String name;
    @Remark("电话")
    @Column(length = 11)
    private String phone;
    @Remark("邮箱")
    @Column(length = 30)
    private String email;
    @NotNull
    @Column(nullable = false)
    private Boolean enabled;
    // 扩展字段1
    private String extra1;
    private String extra2;
    private String extra3;


    public SysUser() {
    }

    public SysUser(String id) {
        this.setId(id);
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (dataPermType == null) {
            dataPermType = DataPermType.CHILDREN;
        }
        if (enabled == null) {
            enabled = true;
        }
    }

    @Override
    public String toString() {
        return name + " " + phone;
    }


}

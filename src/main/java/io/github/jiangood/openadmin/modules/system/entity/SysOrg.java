package io.github.jiangood.openadmin.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.lang.annotation.Remark;
import io.github.jiangood.openadmin.lang.tree.TreeNode;
import io.github.jiangood.openadmin.framework.data.DBConstants;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.List;

/**
 * 系统组织机构表
 */
@Remark("组织机构")
@Getter
@Setter
@Entity
@FieldNameConstants
public class SysOrg extends BaseEntity implements TreeNode<SysOrg> {


    @Transient
    List<SysOrg> children;
    /**
     * 父id, 如果是根节点，则为空
     */
    private String pid;
    /**
     * 名称
     */
    @NotNull
    private String name;
    /**
     * 排序
     */
    private Integer seq;
    @Column(nullable = false)
    private Boolean enabled;
    /***
     * 为了扩展OrgType,这里就不用枚举了，需要扩展时，覆盖OrgType枚举类即可
     */
    @NotNull
    private Integer type;
    // 部门领导
    @ManyToOne
    private SysUser leader;
    // 扩展字段1
    private String extra1;
    private String extra2;
    private String extra3;


    /**
     * 第三方系统的唯一标识符
     */
    @JsonIgnore
    @Column(length = DBConstants.LEN_ID)
    private String thirdId;

    /**
     * 第三方系统的父级唯一标识符，常用于OAuth集成，统一认证等
     */
    @JsonIgnore
    @Column(length = DBConstants.LEN_ID)
    private String thirdPid;

    /**
     * 第三方系统的编码
     */
    @JsonIgnore
    @Column(length = DBConstants.LEN_CODE)
    private String thirdCode;


    public SysOrg() {
    }

    public SysOrg(String id) {
        this.setId(id);
    }

    @Transient
    public boolean isDept() {
        if (type == null) {
            return false;
        }
        return type == OrgType.TYPE_DEPT;
    }


    @Override
    public String toString() {
        return name;
    }


    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
    }
}

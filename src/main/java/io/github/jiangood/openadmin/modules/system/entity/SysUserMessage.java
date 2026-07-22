package io.github.jiangood.openadmin.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "sys_user_message")
@FieldNameConstants
public class SysUserMessage extends BaseEntity {


    @Column(length = 50)
    private String title;


    @Column(length = 10000)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private SysUser user;


    @Column(name = "is_read")
    private Boolean read;

    private Date readTime;


}

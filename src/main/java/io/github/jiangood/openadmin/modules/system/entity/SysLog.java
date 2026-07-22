package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.DBConstants;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * 系统操作日志表
 */
@Getter
@Setter
@Entity
@FieldNameConstants
@Table(name = "sys_log", indexes = {@Index(columnList = "operation")})
public class SysLog extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String operation;


    @Lob
    private String params;

    @Lob
    private String result;


    private Boolean success;


    @Lob
    private String error;

    /**
     * 执行耗时 (毫秒)
     */
    private Integer duration;

    /**
     * 操作用户的ID (关联系统用户表)
     */
    @Column(length = DBConstants.LEN_ID)
    private String userId;

    /**
     * 操作用户名
     */
    @Column(length = 50)
    private String username;


    @Column(length = 64)
    private String ip;

    private Date operationTime;

}

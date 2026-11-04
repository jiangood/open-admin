package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.DBConstants;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 系统操作日志表
 */
@Getter
@Setter
@Entity
@FieldNameConstants
@Table(name = "sys_log", indexes = {
        @Index(columnList = "operation"),
        // 个人中心"最近操作"按用户 + 时间分页查询
        @Index(columnList = "userId, operationTime")
})
public class SysLog extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String operation;

    /**
     * 操作详情，人类可读，例如「删除用户：张三」。
     * <p>
     * 由 {@code @Log.detail} 模板或 {@code OperationLogContext} 生成；旧数据可能为空。
     */
    @Column(length = 500)
    private String detail;


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

    private LocalDateTime operationTime;

}

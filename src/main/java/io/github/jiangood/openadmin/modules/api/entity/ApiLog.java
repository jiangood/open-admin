package io.github.jiangood.openadmin.modules.api.entity;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("接口访问记录")
@Entity
@FieldNameConstants
@Getter
@Setter
@Table(name = "sys_api_log")
public class ApiLog extends BaseEntity {

    @Remark("时间戳")
    @Column(length = 50)
    private Long timestamp;


    @Remark("接口")
    @Column(length = 100)
    private String url;


    @Column(length = 15)
    private String ip;

    @Column(length = 100)
    private String ipLocation;

    @Remark("执行时间")
    private Long executionTime;

    @Remark("接口账户")
    @Column(length = 50)
    private String accountName;

    @Column(length = 32)
    private String accountId;


}

package io.github.jiangood.as.modules.system.entity;

import io.github.jiangood.as.common.tools.annotation.Remark;
import io.github.jiangood.as.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;


/**
 * 参数配置
 */
@Slf4j
@Remark("系统配置")
@Getter
@Setter
@Entity
@FieldNameConstants
@ToString
@Table(name = "sys_config_value")
public class SysConfig extends BaseEntity {

    @Column(length = 64, unique = true)
    private String code;


    @Column(length = 2048)
    private String value;

}

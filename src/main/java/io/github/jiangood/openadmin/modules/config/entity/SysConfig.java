package io.github.jiangood.openadmin.modules.config.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * 系统配置的运行时覆盖值。
 *
 * <p>只保存用户改过的项；没有记录的配置项按 {@code application.yml} 和代码默认值解析。
 * 删除记录即恢复默认。</p>
 */
@Remark("系统配置")
@Getter
@Setter
@Entity
@Table(name = "sys_config")
@FieldNameConstants
public class SysConfig extends BaseEntity {

    @Remark("配置键")
    @Column(name = "config_key", nullable = false, unique = true, length = 128)
    private String configKey;

    @Remark("配置值")
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
}

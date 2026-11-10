package io.github.jiangood.openadmin.modules.system.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * 浏览器指纹备注。
 * <p>
 * 指纹由前端生成并写入操作日志，本表为人工维护的备注信息（如「张三的办公电脑-Chrome」），
 * 按指纹唯一，跨日志复用。指纹可被客户端伪造，备注仅作辅助识别，不能作为安全凭证。
 */
@Getter
@Setter
@Entity
@FieldNameConstants
@Table(name = "sys_browser_fingerprint")
public class SysBrowserFingerprint extends BaseEntity {

    /**
     * 浏览器指纹，与 sys_log.fingerprint 对应
     */
    @Column(nullable = false, unique = true, length = 64)
    private String fingerprint;

    /**
     * 备注名，如「张三的办公电脑」
     */
    @Column(length = 100)
    private String name;

    /**
     * 补充说明
     */
    @Column(length = 500)
    private String remark;

}

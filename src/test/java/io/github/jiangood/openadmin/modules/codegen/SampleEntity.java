package io.github.jiangood.openadmin.modules.codegen;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.framework.file.FileField;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SampleEntity extends BaseEntity {

    @Remark("姓名")
    @Column(nullable = false, length = 100)
    private String name;

    @Remark("状态")
    @Enumerated(EnumType.STRING)
    private SampleStatus status;

    @Remark("类型")
    @Enumerated(EnumType.STRING)
    private PlainType type;

    @Remark("数量")
    private Integer qty;

    @Remark("金额")
    private BigDecimal amount;

    @Remark("业务日期")
    private LocalDate bizDate;

    @Remark("发生时间")
    private LocalDateTime happenAt;

    @Remark("启用")
    private Boolean enabled;

    @Remark("备注")
    @Lob
    private String remark;

    @Remark("附件")
    @FileField
    private String attachment;

    @Remark("头像")
    @FileField
    private String avatar;

    @Remark("详情")
    @FileField(html = true)
    private String detail;

    @DictType(code = "sampleStatus", label = "示例状态")
    public enum SampleStatus {
        @DictItem(label = "启用", color = "GREEN")
        ENABLED,
        @DictItem(label = "停用", color = "RED")
        DISABLED
    }

    public enum PlainType {
        A, B
    }
}

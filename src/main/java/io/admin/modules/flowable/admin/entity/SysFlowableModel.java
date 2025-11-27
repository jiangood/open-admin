package io.admin.modules.flowable.admin.entity;





import io.admin.common.utils.ann.Remark;
import io.admin.framework.data.converter.BaseToListConverter;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@FieldNameConstants
public class SysFlowableModel extends BaseEntity {

    /**
     * 编码, 流程的key
     */
    @Remark("编码")
    @NotNull
    private String code;


    @Remark("名称")
    private String name;


    @Column(columnDefinition = "blob")
    private String content;



}

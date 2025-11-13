package io.admin.modules.ureport.provider;

import io.admin.framework.data.DBConstants;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "sys_ureport")
@FieldNameConstants
public class UReport extends BaseEntity {


    // 文件名称
    @NotNull
    @Column(length = DBConstants.LEN_NAME,unique = true)
    String file;


    @Lob
    String content;

    Date updateTime;
}

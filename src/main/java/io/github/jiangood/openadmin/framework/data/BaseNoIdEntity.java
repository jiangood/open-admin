package io.github.jiangood.openadmin.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jiangood.openadmin.framework.data.DBConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler","fieldHandler"}, ignoreUnknown = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseNoIdEntity implements Persistable<String> {

    @Schema(description = "创建者", hidden = true)
    @CreatedBy
    @Column(length = DBConstants.LEN_ID, updatable = false)
    private String createBy;

    @Schema(description = "创建时间", hidden = true)
    @CreatedDate
    @Column(updatable = false)
    private Date createTime;

    @Schema(description = "更新者", hidden = true)
    @LastModifiedBy
    @Column(length = DBConstants.LEN_ID)
    private String updateBy;

    @Schema(description = "更新时间", hidden = true)
    @LastModifiedDate
    private Date updateTime;

    @Schema(description = "删除标志 true/false 删除/未删除", hidden = true)
    private Boolean deleteFlag;
}

package io.github.jiangood.openadmin.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler","fieldHandler"}, ignoreUnknown = true)
@EntityListeners(AuditingEntityListener.class)

public abstract class BaseNoIdEntity implements Persistable<String> {

    @CreatedBy
    @Column(length = DBConstants.LEN_ID, updatable = false)
    private String createUser;

    @CreatedDate
    @Column(updatable = false)
    private Date createTime;

    @LastModifiedBy
    @Column(length = DBConstants.LEN_ID)
    private String updateUser;

    @LastModifiedDate
    private Date updateTime;


}

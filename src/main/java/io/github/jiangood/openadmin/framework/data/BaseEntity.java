package io.github.jiangood.openadmin.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jiangood.openadmin.util.IdTool;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler","fieldHandler"}, ignoreUnknown = true)
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id", callSuper = false)
public abstract class BaseEntity implements Persistable<String>, Serializable {

    @Id
    @Column(length = DBConstants.LEN_ID)
    private String id;

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

    @PrePersist
    public void ensureId() {
        if (id == null) {
            id = IdTool.uuidV7();
        }
    }

    @JsonIgnore
    @Transient
    public boolean isNew() {
        return null == getId();
    }

}

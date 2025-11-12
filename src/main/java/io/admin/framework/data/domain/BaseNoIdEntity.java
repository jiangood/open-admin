package io.admin.framework.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.admin.framework.data.id.ann.GeneratePrefixedSequence;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseNoIdEntity implements Persistable<String> {

    @CreatedDate
    @Column(updatable = false)
    private Date createTime;

    @LastModifiedDate
    private Date updateTime;



}

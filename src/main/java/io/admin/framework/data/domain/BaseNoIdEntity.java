package io.admin.framework.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseNoIdEntity {









    @CreatedDate
    @Column(updatable = false)
    private Date createTime;




    @LastModifiedDate
    private Date updateTime;



    @org.springframework.data.annotation.Version
    @Column(columnDefinition = "bigint default 0") // 建议：仅用于提示数据库建表时设置默认值
    private Integer lockVersion;

}

package io.github.jiangood.openadmin.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  ID 需要手动设置（非自动生成）
 */
@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler","fieldHandler"}, ignoreUnknown = true)
@EqualsAndHashCode(of = "id", callSuper = false)
public abstract class BaseManualIdEntity extends BaseNoIdEntity implements Serializable {

    @Id
    @Column(length = DBConstants.LEN_ID)
    private String id;

    @JsonIgnore
    @Transient
    public boolean isNew() {
        return null == getId();
    }

}

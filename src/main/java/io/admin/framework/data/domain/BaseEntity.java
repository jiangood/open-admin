package io.admin.framework.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.admin.framework.data.DBConstants;
import io.admin.framework.data.id.ann.GenerateUuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@EqualsAndHashCode(of = "id", callSuper = false)
public abstract class BaseEntity extends BaseNoIdEntity implements Serializable {


    @Id
    @GenerateUuidV7
    @Column(length = DBConstants.LEN_ID)
    private String id;

    @JsonIgnore
    @Transient
    public boolean isNew() {
        return null == getId();
    }

}

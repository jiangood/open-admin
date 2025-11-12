package io.admin.framework.data.domain;

import com.fasterxml.jackson.annotation.*;
import io.admin.common.utils.ann.Remark;
import io.admin.framework.data.DBConstants;
import io.admin.framework.data.id.ann.GenerateUuid7;
import io.admin.framework.perm.SecurityUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String>, Serializable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATE_TIME = "createTime";
    public static final String FIELD_CREATE_USER = "createUser";
    public static final String FIELD_UPDATE_TIME = "updateTime";
    public static final String FIELD_UPDATE_USER = "updateUser";


    public static final String[] BASE_ENTITY_FIELDS = new String[]{FIELD_ID, FIELD_CREATE_TIME, FIELD_CREATE_USER, FIELD_UPDATE_TIME, FIELD_UPDATE_USER, "lockVersion", "_tmpId"};


    public BaseEntity() {
    }

    public BaseEntity(String id) {
        this.id = id;
    }



    @Id
    @GenerateUuid7
    @Column(length = DBConstants.LEN_ID)
    private String id;


    @Column(updatable = false)
    @Remark("创建时间")
    private Date createTime;


    @Remark("创建人ID")
    @Column(length = DBConstants.LEN_ID, updatable = false)
    private String createUser;


    private Date updateTime;


    @Remark("更新人ID")
    @Column(length = DBConstants.LEN_ID)
    private String updateUser;

    /// ===== 乐观锁字段 =====
/*    @Version
    @Column(columnDefinition = "bigint default 0") // 建议：仅用于提示数据库建表时设置默认值
    private Integer lockVersion;*/


    /**
     * 动态字段，处理实体中不包含的字段
     * 例如状态字段 status, 转成json希望动态增加字段 statusLabel
     */
    @Setter(AccessLevel.NONE) // lombok不生成setter
    @Transient
    @JsonAnySetter
    private Map<String, Object> extData = new HashMap<>();


    @JsonAnyGetter
    public Map<String, Object> getExtData() {
        return extData;
    }

    /**
     * 加入额外字段
     *
     * @param key
     * @param value
     */
    public void putExtData(String key, Object value) {
        extData.put(key, value);
    }




    @PrePersist
    public void prePersist() {
        this.prePersistOrUpdate();

        // 有些异步保存的数据，时间上有些许差异。 可提前设置createTime，防止差异发生
        Date now = this.createTime == null ? new Date() : this.createTime;

        this.createTime = now;
        this.updateTime = now;

        if (this.createUser == null && SecurityUtils.getSubject() != null) {
            String userId = SecurityUtils.getSubject().getId();
            this.updateUser = this.createUser = userId;
        }
//        if (this.lockVersion == null) {
//            this.lockVersion = 0;
//        }
    }

    /**
     * 修改前调用
     * 注意：如果修改前后属性无变化，jpa不会入库，也就不会调用本方法
     */
    @PreUpdate
    public void preUpdate() {
        this.prePersistOrUpdate();
        this.updateTime = new Date();
        if (this.updateUser == null && SecurityUtils.getSubject() != null) {
            this.updateUser = SecurityUtils.getSubject().getId();
        }
    }


    public void prePersistOrUpdate() {
    }


    @JsonIgnore
    @Transient
    public boolean isNew() {
        return null == getId();
    }





}

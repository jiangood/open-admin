package io.github.jiangood.openadmin.modules.api.entity;

import io.github.jiangood.openadmin.common.tools.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.converter.ToListConverter;
import io.github.jiangood.openadmin.framework.data.domain.BaseEntity;
import io.github.jiangood.openadmin.framework.validator.ValidateIpv4;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Remark("接口账户")
@Entity
@FieldNameConstants
@Getter
@Setter
@Table(name = "sys_api_account")
public class ApiAccount extends BaseEntity {


    @Remark("名称")
    @Column(length = 50)
    private String name;

    @Remark("准入IP")
    @ValidateIpv4
    private String accessIp;


    @Column(unique = true, length = 32)
    private String appId;

    @Column(unique = true, length = 32)
    private String appSecret;


    @Remark("状态")
    @NotNull(message = "状态不能为空")
    private Boolean enable;


    @Remark("有效期")
    private Date endTime;


    @Lob
    @Convert(converter = ToListConverter.class)
    private List<String> perms = new ArrayList<>();


}

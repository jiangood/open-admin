package io.github.jiangood.openadmin.framework.data;

import io.github.jiangood.openadmin.lang.annotation.Remark;
import lombok.Data;

import java.util.Date;

@Data
public class BaseDto {

    private String id;


    @Remark("创建时间")
    private Date createTime;


    @Remark("创建人ID")
    private String createUser;


    private Date updateTime;


    @Remark("更新人ID")
    private String updateBy;

    private Boolean deleteFlag;
}

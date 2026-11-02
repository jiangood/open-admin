package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心"最近操作"列表行。
 * <p>
 * 不返回 params/result/error，避免把业务数据或内部异常信息回显到个人中心。
 */
@Data
public class UserCenterLogVO {

    private String id;

    /** 操作名称，如"用户-创建" */
    private String operation;

    /** 是否成功 */
    private Boolean success;

    /** 执行耗时（毫秒） */
    private Integer duration;

    private String ip;

    private LocalDateTime operationTime;
}

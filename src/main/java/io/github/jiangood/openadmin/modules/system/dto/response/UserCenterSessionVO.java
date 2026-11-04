package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心「登录设备」列表行。
 * <p>
 * 只暴露当前用户自己需要的字段，真实 sessionId、userId 等内部标识不下发给前端。
 */
@Data
public class UserCenterSessionVO {

    /**
     * 对外暴露的随机标识，用于下线指定设备
     */
    private String id;

    private String ip;

    /**
     * IP 是否属于内网地址，用于前端展示「内网」标记
     */
    private boolean innerIp;

    private String userAgent;

    private LocalDateTime loginTime;

    private LocalDateTime lastActiveTime;

    /**
     * 是否当前请求所在的会话
     */
    private boolean current;

}

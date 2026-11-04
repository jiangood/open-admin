package io.github.jiangood.openadmin.framework.auth.online;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 在线用户（一个登录会话一行）。
 * <p>
 * 数据直接来自登录时的 {@code LoginUser} 与 {@code HttpSession}，不查库，避免每次刷新页面产生额外查询。
 */
@Getter
@Setter
public class OnlineUser {

    /**
     * 对外暴露的随机标识，避免把真实 sessionId 泄露给前端
     */
    private String id;

    /**
     * 真实 sessionId，仅服务端使用
     */
    @JsonIgnore
    private String sessionId;

    private String userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    private String unitId;
    private String unitName;
    private String orgId;
    private String orgName;

    private String ip;

    /**
     * IP 是否属于内网地址，由查询时实时计算，用于前端展示「内网」标记
     */
    private boolean innerIp;

    private String userAgent;

    private LocalDateTime loginTime;

    /**
     * 最后活跃时间，由 SessionRegistry 的 lastRequest 实时计算，不落库
     */
    private LocalDateTime lastActiveTime;

    /**
     * 最近一次操作名称，来自操作日志表 sys_log
     */
    private String lastOperation;

    /**
     * 最近一次操作时间，来自操作日志表 sys_log
     */
    private LocalDateTime lastOperationTime;

    /**
     * 最近一次操作的友好时间描述（如「5分钟前」），由 {@code FriendlyTool.getPastTime} 计算
     */
    private String lastOperationTimeText;

    /**
     * 是否当前请求所在的会话（前端据此隐藏「强制下线」）
     */
    private boolean current;

}

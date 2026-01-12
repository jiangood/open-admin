package io.github.jiangood.as.modules.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1 表示账号相关
 * 2 表示接口相关
 * 3 表示权限相关
 * <p>
 * 5 表示服务器，时间戳等异常
 */
@AllArgsConstructor
@Getter
public enum ApiErrorCode {


    ACC_NOT_FOUND(1404, "账号不存在"),
    ACC_NOT_FORBIDDEN(1401, "账号已禁用"),
    ACC_EXPIRE(1410, "账号已过期"),
    ACC_IP(1411, "IP访问限制"),


    RES_NOT_FOUND(2404, "接口不存在"),

    PERM_NOT_FOUND(3404, "未授权"),
    PERM_DISABLE(3401, "权限已被禁用"),

    GLOBAL_ERROR(5000, "服务器错误"),
    TIME_BIG_DIF(5001, "请求时间戳与服务器时间差值过大"),

    SIGN_ERROR(5002, "签名错误");

    int code;
    String message;


}

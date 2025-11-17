package io.admin.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = SysProp.CONFIG_PREFIX)
@Data
public class SysProp {


    public static final String CONFIG_PREFIX = "sys";


    /**
     * 是否开启验证码登录
     */
    private boolean captcha = false;
    /**
     * 验证码类型
     */
    private CaptchaType captchaType;

    public enum CaptchaType {
        MATH, RANDOM
    }

    /**
     * 最大并发会话数， 即同时登录用户数
     */
    private int maximumSessions = 1;

    /**
     * 系统标题
     */
    private String title = "管理系统";
    private String loginBoxBottomTip = "当前非涉密网络，严禁传输处理涉密信息";

    /**
     * 不经过xss的路径, 如 /aa/*
     */
    private List<String> xssExcludePathList;

    private List<String> loginExcludePathPatterns;

    /**
     * 缓存目录
     */
    private String dataFileDir = "/data/";


    /**
     * 允许上传文件的后缀， 如 docx
     */
    private String allowUploadFiles = "docx,xlsx,pdf,png,jpg,mp3,mp4,wav";


    /**
     * session空闲时间（分钟），超过该时间则登录失效
     */
    private int sessionIdleTime = 180;

    /***
     * 登录锁定时间（分钟）
     */
    private int loginLockMinutes = 5;


    /**
     * 登录异常最大次数， 超过则锁定
     */
    private int loginLockMaxAttempts = 10;

    /**
     * 本地上传文件路径
     */
    private String fileUploadPath = "/home/files";


    /**
     * 定时任务，全局开关 , 某些情况如开发时，可按需关闭
     */
    private boolean jobEnable = true;

    /**
     * 重置管理员密码
     */
    private String resetAdminPwd;

    private boolean printException;
}

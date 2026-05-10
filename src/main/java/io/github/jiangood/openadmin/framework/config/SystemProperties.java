package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.RequestTool;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = SystemProperties.CONFIG_PREFIX)
@Data
@Validated
public class SystemProperties {


    public static final String CONFIG_PREFIX = "sys";


    /**
     * 请求基础地址
     * 非必填，在复杂情况下使用，可用于拼接完整请求地址,
     */
    private String baseUrl;


    /**
     * 是否开启验证码登录
     */
    private boolean captcha = true;
    /**
     * 最大并发会话数， 即同时登录用户数
     */
    private int maximumSessions = 1;
    /**
     * 系统标题
     */
    @NotBlank(message = "请配置系统标题")
    private String title = "管理系统";
    /**
     * 是否显示logo
     */
    private boolean showLogo = true;
    /**
     * logo图片地址,
     * 可简单放到目录 static/admin/public/
     * public 表示不用认证也能访问，如 /admin/public/logo.jpg
     */
    private String logoUrl = "/admin/public/logo.jpg";
    private String loginBoxBottomTip = "当前非涉密网络，严禁传输处理涉密信息";
    private String copyright = "Copyright © 2023-2024  All Rights Reserved";
    /**
     * 登录背景图
     */
    private String loginBackground;
    /**
     * 是否开启水印
     */
    private boolean waterMark = false;
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
     * 定时任务，全局开关 , 某些情况如开发时，可按需关闭
     */
    private boolean jobEnable = true;
    /**
     * 重置管理员密码
     */
    private String resetAdminPwd;

    /**
     *  是否打印全局捕获的异常， 通常指web请求异常
     */
    private boolean printGlobalException = true;

    private String defaultPassword = RandomUtil.randomString(16);

    /**
     * 文件存储配置
     */
    private FileStorage file = new FileStorage();

    @Data
    public static class FileStorage {

        /**
         * 存储类型: local, minio
         */
        private String storeType = "local";

        /**
         * 本地上传文件路径
         */
        private String uploadPath = "/home/files";

        /**
         * 允许上传文件的后缀，如 docx
         */
        private String allowUpload = "docx,xlsx,pdf,png,jpg,jpeg,mp3,mp4,wav";

        /**
         * Minio 配置
         */
        private Minio minio = new Minio();

        @Data
        public static class Minio {
            private String url;
            private String accessKey;
            private String secretKey;
            private String bucketName;
        }
    }



    public String getBaseUrl() {
        String url = this.baseUrl;
        if (StrUtil.isEmpty(url)) {
            url = RequestTool.getBaseUrl(RequestTool.currentRequest());
        }
        return url;
    }

}

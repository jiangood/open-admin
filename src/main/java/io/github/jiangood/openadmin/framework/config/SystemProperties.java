package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.util.RequestTool;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
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
     * 允许的跨域来源，仅在 prod 环境生效（dev 环境允许通配符）
     */
    private List<String> allowedOrigins;
    /**
     * 最大并发会话数， 即同时登录用户数
     */
    private int maximumSessions = 1;
    /**
     * 系统标题
     */
    @NotBlank(message = "请配置系统标题")
    private String title = "管理系统";
    private String loginBoxBottomTip; // "当前非涉密网络，严禁传输处理涉密信息";
    private String copyright; // = "Copyright © 2023-2024  All Rights Reserved";
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
     *  是否打印全局捕获的异常， 通常指web请求异常
     */
    private boolean printGlobalException = true;

    /**
     * 数据迁移时是否直接删除旧表（true=删除, false=重命名备份）
     */
    private boolean migrationDropOldTables = false;

    /**
     * 文件存储配置
     */
    private FileStorage file = new FileStorage();

    @Data
    public static class FileStorage {

        public enum StoreType {
            LOCAL, S3, CUSTOM
        }

        /**
         * 存储类型: LOCAL / S3；自定义实现请注册 @Bean @Primary FileOperator
         */
        private StoreType storeType = StoreType.LOCAL;

        /**
         * 本地上传文件路径
         */
        private String uploadPath = "/home/files";

        /**
         * 允许上传文件的后缀，如 docx
         */
        private String allowUpload = "docx,xlsx,pdf,png,jpg,jpeg,webp,mp3,mp4,wav,txt";

        /**
         * 未认领文件自动清理时间（分钟），默认 120（2 小时）
         */
        private int cleanUnclaimedMinutes = 120;

        /**
         * S3 兼容存储配置（支持 AWS S3 / Minio / Cloudflare R2 等）
         */
        private S3 s3 = new S3();

        @Data
        public static class S3 {
            private String endpoint;
            private String region = "us-east-1";
            private String accessKey;
            private String secretKey;
            private String bucketName;
            private Boolean pathStyleAccess = true;
        }
    }



    public String getBaseUrl() {
        String url = this.baseUrl;
        if (CharSequenceUtil.isEmpty(url)) {
            url = RequestTool.getBaseUrl(RequestTool.currentRequest());
        }
        return url;
    }

}

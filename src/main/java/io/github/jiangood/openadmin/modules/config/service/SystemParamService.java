package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.config.dto.SystemParamView;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 只读系统参数。
 *
 * <p>展示启动期配置（application.yml / 代码默认值）的当前生效值，
 * 便于管理员在「系统管理 → 系统参数」页面确认部署状态；这些值不能在页面修改。</p>
 */
@Service
@RequiredArgsConstructor
public class SystemParamService {

    private final SystemProperties systemProperties;
    private final Environment environment;

    public List<SystemParamView> items() {
        SystemProperties.FileStorage file = systemProperties.getFile();
        List<SystemParamView> items = new ArrayList<>();
        add(items, "sys.maximum-sessions", "最大并发会话数",
                String.valueOf(systemProperties.getMaximumSessions()));
        add(items, "sys.max-sessions-prevents-login", "并发超限策略",
                systemProperties.isMaxSessionsPreventsLogin() ? "阻止新登录（旧会话继续有效）" : "踢出旧会话");
        add(items, "sys.session-idle-time", "会话空闲超时",
                systemProperties.getSessionIdleTime() + " 分钟");
        add(items, "sys.login-lock-max-attempts", "登录失败上限",
                systemProperties.getLoginLockMaxAttempts() + " 次");
        add(items, "sys.login-lock-minutes", "登录锁定时长",
                systemProperties.getLoginLockMinutes() + " 分钟");
        add(items, "sys.file.store-type", "文件存储方式",
                file.getStoreType() == SystemProperties.FileStorage.StoreType.S3 ? "S3 对象存储" : "本地存储");
        add(items, "sys.file.upload-path", "上传目录", blankToDash(file.getUploadPath()));
        add(items, "sys.file.clean-temp-minutes", "临时文件清理",
                file.getCleanTempMinutes() + " 分钟");
        add(items, "sys.data-file-dir", "数据目录", blankToDash(systemProperties.getDataFileDir()));
        add(items, "logging.file.path", "日志目录",
                blankToDash(environment.getProperty("logging.file.path")));
        return items;
    }

    private static void add(List<SystemParamView> items, String key, String label, String value) {
        items.add(new SystemParamView(key, label, value));
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}

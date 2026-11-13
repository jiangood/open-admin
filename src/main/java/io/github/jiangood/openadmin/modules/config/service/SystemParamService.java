package io.github.jiangood.openadmin.modules.config.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.config.dto.SystemParamView;
import io.github.jiangood.openadmin.util.DurationTool;
import io.github.jiangood.openadmin.util.VersionTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 只读系统参数。
 *
 * <p>展示启动期配置（application.yml / 代码默认值）的当前生效值、运行时状态（启动时间、数据库连接与连接池用量）
 * 以及框架与应用的版本信息，便于管理员在「系统管理 → 系统参数」页面确认部署状态、定位连接不足等问题；
 * 这些值不能在页面修改。</p>
 */
@Service
@RequiredArgsConstructor
public class SystemParamService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 进程启动时间：取 JVM 启动时刻，重启前后对比即可确认是否已重新部署 */
    private static final long START_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();

    /** JDBC URL 里的密码：mysql/postgres 等 `//user:pass@host` 形式 */
    private static final Pattern URL_PASSWORD = Pattern.compile("(//[^/@:]+):[^/@]*@");

    /** JDBC URL 里的密码：`password=xxx` 形式（SQL Server 的 `;password=xxx`、查询参数） */
    private static final Pattern PARAM_PASSWORD = Pattern.compile("(?i)(password=)[^;&]*");

    private final SystemProperties systemProperties;
    private final Environment environment;
    /**
     * 用 ObjectProvider 而不是直接注入 DataSource：业务项目接入多数据源（如 dynamic-datasource）时，
     * 按类型注入会因候选 bean 不唯一导致启动失败，这里取不到就静默跳过连接信息。
     */
    private final ObjectProvider<DataSource> dataSourceProvider;

    public List<SystemParamView> items() {
        SystemProperties.FileStorage file = systemProperties.getFile();
        DataSource dataSource = dataSourceProvider.getIfUnique();
        List<SystemParamView> items = new ArrayList<>();
        // 运行时状态排在前面：这两项会随服务运行变化，页面定时刷新
        add(items, SystemParamView.GROUP_RUNTIME, "sys.start-time", "系统启动时间", startTime());
        String pool = poolUsage(dataSource);
        if (pool != null) {
            add(items, SystemParamView.GROUP_RUNTIME, "spring.datasource.hikari", "连接池", pool);
        }
        add(items, SystemParamView.GROUP_STARTUP, "spring.datasource.url", "数据库连接", jdbcUrl(dataSource));
        add(items, SystemParamView.GROUP_STARTUP, "sys.maximum-sessions", "最大并发会话数",
                String.valueOf(systemProperties.getMaximumSessions()));
        add(items, SystemParamView.GROUP_STARTUP, "sys.max-sessions-prevents-login", "并发超限策略",
                systemProperties.isMaxSessionsPreventsLogin() ? "阻止新登录（旧会话继续有效）" : "踢出旧会话");
        add(items, SystemParamView.GROUP_STARTUP, "sys.session-idle-time", "会话空闲超时",
                systemProperties.getSessionIdleTime() + " 分钟");
        add(items, SystemParamView.GROUP_STARTUP, "sys.login-lock-max-attempts", "登录失败上限",
                systemProperties.getLoginLockMaxAttempts() + " 次");
        add(items, SystemParamView.GROUP_STARTUP, "sys.login-lock-minutes", "登录锁定时长",
                systemProperties.getLoginLockMinutes() + " 分钟");
        add(items, SystemParamView.GROUP_STARTUP, "sys.file.store-type", "文件存储方式",
                file.getStoreType() == SystemProperties.FileStorage.StoreType.S3 ? "S3 对象存储" : "本地存储");
        add(items, SystemParamView.GROUP_STARTUP, "sys.file.upload-path", "上传目录",
                blankToDash(file.getUploadPath()));
        add(items, SystemParamView.GROUP_STARTUP, "sys.file.clean-temp-minutes", "临时文件清理",
                file.getCleanTempMinutes() + " 分钟");
        add(items, SystemParamView.GROUP_STARTUP, "sys.data-file-dir", "数据目录",
                blankToDash(systemProperties.getDataFileDir()));
        add(items, SystemParamView.GROUP_STARTUP, "logging.file.path", "日志目录",
                blankToDash(environment.getProperty("logging.file.path")));
        // 版本信息：打包运行附带构建时间，开发模式只有版本号
        add(items, SystemParamView.GROUP_VERSION, "sys.framework-version", "框架版本",
                withBuildTime(VersionTool.getFrameworkVersion(), VersionTool.getFrameworkBuildTime()));
        add(items, SystemParamView.GROUP_VERSION, "sys.app-version", "应用版本",
                withBuildTime(VersionTool.getApplicationVersion(), VersionTool.getApplicationBuildTime()));
        return items;
    }

    /** 启动时间附上已运行时长，方便判断服务是否需要重启 */
    private static String startTime() {
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(START_TIME), ZoneId.systemDefault());
        Duration uptime = Duration.ofMillis(Math.max(0, System.currentTimeMillis() - START_TIME));
        String text = DurationTool.format(uptime);
        if (text == null || text.isBlank()) {
            text = "不到 1 秒";
        }
        return DATE_TIME.format(start) + "（已运行 " + text + "）";
    }

    /** 优先取连接池里的实际 URL（已展开占位符），拿不到再回退到配置项；密码类参数一律脱敏 */
    private String jdbcUrl(DataSource dataSource) {
        String url = dataSource instanceof HikariDataSource hikari ? hikari.getJdbcUrl() : null;
        if (url == null || url.isBlank()) {
            url = environment.getProperty("spring.datasource.url");
        }
        return blankToDash(maskJdbcUrl(url));
    }

    /**
     * 连接池最关键的四个数：活动 / 空闲 / 上限 / 等待线程。
     * 等待线程长期大于 0 或活动数顶到上限，说明连接池偏小或连接未释放。
     */
    private String poolUsage(DataSource dataSource) {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return null;
        }
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        if (pool == null) {
            return null;
        }
        return "活动 " + pool.getActiveConnections()
                + " / 空闲 " + pool.getIdleConnections()
                + " / 上限 " + hikari.getMaximumPoolSize()
                + " / 等待 " + pool.getThreadsAwaitingConnection();
    }

    static String maskJdbcUrl(String url) {
        if (url == null) {
            return null;
        }
        return URL_PASSWORD.matcher(PARAM_PASSWORD.matcher(url).replaceAll("$1***")).replaceAll("$1:***@");
    }

    private static void add(List<SystemParamView> items, String group, String key, String label, String value) {
        items.add(new SystemParamView(key, label, value, group));
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    /** 版本号附构建时间（精确到秒）：拿不到构建时间（开发模式）时只显示版本号 */
    private static String withBuildTime(String version, String buildTime) {
        if (version == null || version.isBlank()) {
            return null;
        }
        return buildTime == null || buildTime.isBlank() ? version : version + "（" + buildTime + "）";
    }
}

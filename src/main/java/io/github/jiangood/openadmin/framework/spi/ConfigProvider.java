package io.github.jiangood.openadmin.framework.spi;

import io.github.jiangood.openadmin.framework.config.dynamic.ConfigGroup;

import java.util.List;

/**
 * 系统配置项声明扩展点。
 *
 * <p>框架与业务模块都通过实现本接口声明自己负责的配置项；实现类注册为 Spring Bean 后，
 * 配置项会自动出现在「系统管理 → 系统配置」页面。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * @Component
 * public class CvsConfigProvider implements ConfigProvider {
 *     @Override
 *     public List<ConfigGroup> define() {
 *         return List.of(
 *             Configs.group("cvs.capture", "抓拍", 10)
 *                 .add(Configs.bool("cvs.capture.enabled", "启用抓拍").defaultValue(true))
 *                 .add(Configs.integer("cvs.capture.interval", "抓拍间隔（秒）").defaultValue(10).min(5).max(3600))
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p>注意：key 必须全局唯一，建议以“模块.参数”命名，全部小写、点分隔、段落内允许连字符，
 * 例如 {@code sys.file.allow-upload}。</p>
 */
public interface ConfigProvider {

    List<ConfigGroup> define();
}

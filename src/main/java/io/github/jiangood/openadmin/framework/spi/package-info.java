/**
 * 框架扩展点（SPI）。
 *
 * <p>此包下的接口均为 open-admin 框架定义的扩展点，业务项目可通过实现这些接口
 * 来自定义框架行为。所有实现类需注册为 Spring {@code @Component}，框架通过
 * 组件扫描自动发现并注入。
 *
 * <ul>
 *   <li>{@link io.github.jiangood.openadmin.framework.spi.OrgTypeProvider} — 自定义机构类型</li>
 *   <li>{@link io.github.jiangood.openadmin.framework.spi.FileOperator} — 自定义文件存储</li>
 *   <li>{@link io.github.jiangood.openadmin.framework.spi.StartupHook} — 系统启动钩子</li>
 * </ul>
 */
package io.github.jiangood.openadmin.framework.spi;

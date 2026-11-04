package io.github.jiangood.openadmin.framework.log;

/**
 * 操作日志详情的线程内上下文。
 *
 * <p>{@link Log} 注解的 {@code detail} 适合能直接从方法参数拼出来的场景；
 * 删除、重置密码等只有 id、或需要读取旧值的场景，可以在业务方法里显式设置：</p>
 *
 * <pre>{@code
 * @Log("用户-删除")
 * public AjaxResult delete(@RequestBody IdReq req) {
 *     SysUser user = sysUserService.findById(req.getId()).orElseThrow();
 *     OperationLogContext.setDetail("删除用户「" + user.getName() + "」");
 *     ...
 * }
 * }</pre>
 *
 * <p>切面在 {@code finally} 中恢复进入方法前的状态，嵌套调用不会串数据。</p>
 */
public final class OperationLogContext {

    private static final ThreadLocal<String> DETAIL = new ThreadLocal<>();

    private OperationLogContext() {
    }

    /**
     * 设置本次操作的详情，优先级高于 {@link Log#detail()}。
     *
     * @param detail 人类可读的操作详情；null / 空白视为未设置
     */
    public static void setDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            DETAIL.remove();
        } else {
            DETAIL.set(detail);
        }
    }

    /**
     * 当前线程已设置的详情，供切面读取。
     */
    public static String getDetail() {
        return DETAIL.get();
    }

    /**
     * 进入被 {@link Log} 标注的方法：保存外层详情并清空当前线程。
     *
     * @return 外层详情，供 {@link #restore(String)} 恢复
     */
    static String enter() {
        String previous = DETAIL.get();
        DETAIL.remove();
        return previous;
    }

    /**
     * 离开被 {@link Log} 标注的方法，恢复进入前的详情。
     */
    static void restore(String previous) {
        if (previous == null) {
            DETAIL.remove();
        } else {
            DETAIL.set(previous);
        }
    }
}

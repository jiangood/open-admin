package io.github.jiangood.openadmin.framework.web;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.util.BusinessException;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * CRUD Controller 公共骨架。
 *
 * <p>统一收敛增删改查接口里的固定逻辑：详情返回、分页查询、成功响应，以及
 * 「id 非空 → 确有字段提交 → 记录存在」的更新/删除校验。校验失败抛
 * {@link BusinessException}，由全局异常处理器转换成 {@code AjaxResult.err} 业务文案，
 * 不再被兜底异常处理成「服务器忙」。</p>
 *
 * <p>典型用法：</p>
 * <pre>{@code
 * @GetMapping("page")
 * public AjaxResult page(String searchText, Pageable pageable) {
 *     return CrudOps.page(service, pageable, spec -> {
 *         spec.orLike(searchText, "name");
 *         return spec;
 *     });
 * }
 *
 * @PostMapping("update")
 * public AjaxResult update(@RequestBody Customer input,
 *         @RequestHeader(value = "X-Body-Fields", required = false) List<String> updateFields) {
 *     CrudOps.requireUpdate(input.getId(), updateFields,
 *             () -> service.findById(input.getId()), "记录不存在");
 *     service.save(input, updateFields);
 *     return CrudOps.updated();
 * }
 * }</pre>
 */
public final class CrudOps {

    public static final String MSG_ID_REQUIRED = "id不能为空";
    public static final String MSG_NO_FIELDS = "没有提交任何字段";
    public static final String MSG_RECORD_NOT_FOUND = "记录不存在";

    private CrudOps() {
    }

    /**
     * 详情响应：存在则返回数据，不存在返回业务错误。
     */
    public static AjaxResult info(Optional<?> found, String notFoundMsg) {
        Optional<?> value = found == null ? Optional.empty() : found;
        return value.<AjaxResult>map(data -> AjaxResult.ok().data(data))
                .orElseGet(() -> AjaxResult.err(defaultNotFound(notFoundMsg)));
    }

    /**
     * 分页响应。{@code customizer} 为空时按 {@code service.spec()} 原样查询，
     * 否则在空 Spec 上追加业务条件。
     */
    public static <T extends Persistable<String>> AjaxResult page(BaseService<T> service,
                                                                  Pageable pageable,
                                                                  UnaryOperator<Spec<T>> customizer) {
        Spec<T> spec = service.spec();
        if (customizer != null) {
            spec = customizer.apply(spec);
        }
        return AjaxResult.ok().data(service.findAll(spec, pageable));
    }

    public static AjaxResult created() {
        return AjaxResult.ok().msg("创建成功");
    }

    public static AjaxResult created(Object saved) {
        return created().data(saved);
    }

    public static AjaxResult updated() {
        return AjaxResult.ok().msg("更新成功");
    }

    public static AjaxResult deleted() {
        return AjaxResult.ok().msg("删除成功");
    }

    /**
     * 更新前校验：id 非空 → 确有字段提交 → 记录存在。
     *
     * <p>使用 {@link Supplier} 延迟查询，避免 id 为空时先执行一次无效的
     * {@code findById(null)}，也保证校验失败时不会触碰数据库。</p>
     *
     * @return 数据库中的已有记录
     */
    public static <T> T requireUpdate(String id,
                                      Collection<String> declaredFields,
                                      Supplier<Optional<T>> existingSupplier,
                                      String notFoundMsg) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(MSG_ID_REQUIRED);
        }
        if (declaredFields == null || declaredFields.stream().noneMatch(StringUtils::hasText)) {
            throw new BusinessException(MSG_NO_FIELDS);
        }
        return found(existingSupplier).orElseThrow(() -> new BusinessException(defaultNotFound(notFoundMsg)));
    }

    /**
     * 更新前校验（记录已查询好的场景）。
     */
    public static <T> T requireUpdate(String id,
                                      Collection<String> declaredFields,
                                      Optional<T> existing,
                                      String notFoundMsg) {
        return requireUpdate(id, declaredFields, () -> existing, notFoundMsg);
    }

    /**
     * 删除前校验记录存在。
     *
     * @return 数据库中的已有记录，便于写操作日志详情
     */
    public static <T> T requireDelete(Optional<T> existing, String notFoundMsg) {
        return requireDelete(() -> existing, notFoundMsg);
    }

    /**
     * 删除前校验记录存在（延迟查询）。
     */
    public static <T> T requireDelete(Supplier<Optional<T>> existingSupplier, String notFoundMsg) {
        return found(existingSupplier).orElseThrow(() -> new BusinessException(defaultNotFound(notFoundMsg)));
    }

    private static <T> Optional<T> found(Supplier<Optional<T>> supplier) {
        if (supplier == null) {
            return Optional.empty();
        }
        Optional<T> existing = supplier.get();
        return existing == null ? Optional.empty() : existing;
    }

    private static String defaultNotFound(String notFoundMsg) {
        return StringUtils.hasText(notFoundMsg) ? notFoundMsg : MSG_RECORD_NOT_FOUND;
    }
}

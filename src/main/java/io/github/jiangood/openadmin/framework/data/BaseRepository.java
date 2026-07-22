package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 强制刷新 JPA 实体管理器的持久化上下文到数据库。
     */
    @Transactional
    public void flush();

    /**
     * 更新指定字段：先 find 再更新 (Find-then-Update)。
     * 适用于需要触发 JPA 生命周期回调（如 @PreUpdate）的场景。
     *
     * @param entity         包含更新后字段值的实体对象
     * @param fieldsToUpdate 需要更新的字段名列表
     */
    @Transactional
    public void updateField(T entity, List<String> fieldsToUpdate);

    /**
     * 直接更新指定字段：使用 CriteriaUpdate (Direct Update)。
     * 直接发送 UPDATE SQL，不加载实体，性能更高但不触发 JPA 生命周期回调。
     *
     * @param entity         包含更新后字段值的实体对象
     * @param fieldsToUpdate 需要更新的字段名列表
     */
    @Transactional
    public void updateFieldDirect(T entity, List<String> fieldsToUpdate);

    /**
     * 根据 ID 查询单个实体，不存在时返回 null。
     *
     * @param id 实体 ID
     * @return 实体对象，或 null
     */
    T findOne(ID id);

    /**
     * 根据 ID 数组批量查询实体列表。
     *
     * @param ids ID 数组
     * @return 实体列表
     */
    public List<T> findAllById(ID[] ids);

    /**
     * 刷新实体状态，从数据库重新加载，避免从缓存读取。
     *
     * @param t 需要刷新的实体
     */
    public void refresh(T t);

    /**
     * 根据 ID 查找实体并立即刷新，确保获取数据库最新状态。
     *
     * @param id 实体 ID
     * @return 实体对象，不存在时返回 null
     */
    public T findByIdAndRefresh(ID id);

    // --- 5.1 JpaQuery/字段等值查询 (Custom Query Helpers) ---

    /**
     * 根据单个字段等值查询单个实体。
     *
     * @param key   字段名
     * @param value 字段值
     * @return 匹配的实体，不存在时返回 null
     */
    public T findByField(String key, Object value);

    /**
     * 根据两个字段等值查询单个实体（AND 逻辑）。
     *
     * @param key   第一个字段名
     * @param value 第一个字段值
     * @param key2  第二个字段名
     * @param value2 第二个字段值
     * @return 匹配的实体，不存在时返回 null
     */
    public T findByField(String key, Object value, String key2, Object value2);

    /**
     * 根据三个字段等值查询单个实体（AND 逻辑）。
     *
     * @param key    第一个字段名
     * @param value  第一个字段值
     * @param key2   第二个字段名
     * @param value2 第二个字段值
     * @param key3   第三个字段名
     * @param value3 第三个字段值
     * @return 匹配的实体，不存在时返回 null
     */
    public T findByField(String key, Object value, String key2, Object value2, String key3, Object value3);

    /**
     * 根据单个字段等值查询实体列表。
     *
     * @param key   字段名
     * @param value 字段值
     * @return 匹配的实体列表
     */
    public List<T> findAllByField(String key, Object value);

    /**
     * 创建一个 Spec 动态查询构建器，用于链式构建复杂查询条件。
     *
     * @return Spec 实例
     */
    public default Spec<T> spec() {
        return Spec.of();
    }

    /**
     * 根据两个字段等值查询实体列表（AND 逻辑）。
     *
     * @param key    第一个字段名
     * @param value  第一个字段值
     * @param key2   第二个字段名
     * @param value2 第二个字段值
     * @return 匹配的实体列表
     */
    public List<T> findAllByField(String key, Object value, String key2, Object value2);

    /**
     * 判断字段值是否存在（排除指定 ID 的记录）。
     * 例如修改用户名时，判断新用户名是否已被其他用户使用。
     *
     * @param id        要排除的实体 ID（新增时传 null）
     * @param fieldName 字段名
     * @param value     字段值
     * @return 如果存在返回 true
     */
    public boolean isFieldExist(ID id, String fieldName, Object value);

    /**
     * 判断字段值是否唯一（排除指定 ID 的记录）。
     *
     * @param id        要排除的实体 ID（新增时传 null）
     * @param fieldName 字段名
     * @param value     字段值
     * @return 如果唯一返回 true
     */
    public boolean isUnique(ID id, String fieldName, Object value);

    /**
     * 按示例对象模糊查询列表（字符串字段使用 CONTAINING 匹配）。
     *
     * @param t    示例对象，非空字段作为查询条件
     * @param sort 排序条件
     * @return 匹配的实体列表
     */
    public List<T> findByExampleLike(T t, Sort sort);

    /**
     * 按示例对象模糊分页查询（字符串字段使用 CONTAINING 匹配）。
     *
     * @param t        示例对象，非空字段作为查询条件
     * @param pageable 分页条件
     * @return 分页结果
     */
    public Page<T> findByExampleLike(T t, Pageable pageable);

    /**
     * 查询符合条件的第一条记录。
     *
     * @param c    查询条件
     * @param sort 排序条件
     * @return 匹配的第一个实体，不存在时返回 null
     */
    public T findTop1(Specification<T> c, Sort sort);

    /**
     * 查询符合条件的前 N 条记录。
     *
     * @param size 返回记录数
     * @param c    查询条件
     * @param sort 排序条件
     * @return 实体列表
     */
    public List<T> findTop(int size, Specification<T> c, Sort sort);

    /**
     * 查询指定字段的值列表。
     *
     * @param fieldName 字段名
     * @param c         查询条件
     * @param <R>       字段值类型
     * @return 字段值列表
     */
    public <R> List<R> findField(String fieldName, Specification<T> c);


    // --- 6. 统计与聚合 (Statistics and Aggregation) ---

    /**
     * 分组统计
     * <p>
     * 执行复杂的分组统计查询，支持多字段选择、聚合函数和分组操作
     * <p>
     * <strong>使用示例：</strong>
     * <pre>
     * Spec&lt;User&gt; spec = Spec.&lt;User&gt;of()
     *     .select("username")                           // 选择分组字段
     *     .selectFnc(Spec.Fuc.SUM, "age", "totalAge")   // 计算年龄总和，别名为totalAge
     *     .selectFnc(Spec.Fuc.COUNT, "id", "userCount") // 计算用户数量，别名为userCount
     *     .groupBy("username")                           // 按用户名分组
     *     .orderBy("totalAge", Direction.DESC);          // 按总年龄降序排序
     * 
     * List&lt;Dict&gt; result = userRepository.stats(spec);
     * // 结果格式: [{"username": "admin", "totalAge": 30, "userCount": 1}, ...]
     * </pre>
     *
     * @param spec 包含查询条件、选择字段、聚合函数和分组信息的Specification
     * @return 统计结果列表，每个元素是一个Dict，包含分组字段和统计值
     */
    public List<Dict> stats(Specification<T> spec);

    /**
     * 单结果统计
     * <p>
     * 执行统计查询并确保只返回一个结果
     * <p>
     * <strong>使用场景：</strong>
     * - 计算总记录数
     * - 计算平均值、总和等单一统计值
     * - 其他需要确保只返回一个结果的统计场景
     *
     * @param spec 包含查询条件和统计信息的Specification
     * @return 统计结果Dict，包含统计字段和值
     * @throws IllegalStateException 如果查询返回多个结果
     */
    public Dict statsSingleResult(Specification<T> spec);


    // --- 7. 结果集映射 (Dictionary Mapping) ---

    /**
     * 根据 ID 集合查询并转换为 Map，key 为实体 ID，value 为实体对象。
     *
     * @param ids ID 集合
     * @return Map，key 为 ID，value 为实体
     */
    public Map<ID, T> findMap(Iterable<ID> ids);

    /**
     * 根据查询条件查询并转换为 Map，key 为实体 ID，value 为实体对象。
     *
     * @param spec 查询条件
     * @param sort 排序条件
     * @return Map，key 为 ID，value 为实体
     */
    public Map<ID, T> findMap(Specification<T> spec, Sort sort);

    /**
     * 根据查询条件查询并转换为 Map，使用自定义函数提取 key。
     *
     * @param spec     查询条件
     * @param sort     排序条件
     * @param keyField key 提取函数
     * @return Map，key 由 keyField 决定，value 为实体
     */
    public Map<ID, T> findMap(Specification<T> spec, Sort sort, Function<T, ID> keyField);

    /**
     * 将查询结果的两个字段组装成 Map。
     *
     * @param spec       查询条件
     * @param sort       排序条件
     * @param keyField   key 提取函数
     * @param valueField value 提取函数
     * @param <V>        value 类型
     * @return Map，key 和 value 分别由对应函数决定
     */
    public <V> Map<ID, V> findMap(Specification<T> spec, Sort sort, Function<T, ID> keyField, Function<T, V> valueField);

    /**
     * 根据查询条件查询并转换为 Map，key 为实体 ID，value 为实体列表（用于一对多场景）。
     *
     * @param spec     查询条件
     * @param sort     排序条件
     * @param keyField key 提取函数
     * @return Map，key 为 ID，value 为实体列表
     */
    public Map<ID, List<T>> findMapList(Specification<T> spec, Sort sort, Function<T, ID> keyField);

    /**
     * 获取实体的 ID 值。
     *
     * @param entity 实体对象
     * @return 实体 ID
     */
    public ID getId(T entity);

    // --- 8. Batch Operations --- 

    /**
     * 批量保存实体
     */
    @Transactional
    public List<T> saveAllBatch(Iterable<T> entities);

    /**
     * 批量更新指定字段
     */
    @Transactional
    public void updateFieldBatch(Iterable<T> entities, List<String> fieldsToUpdate);

    /**
     * 批量删除
     */
    @Transactional
    public void deleteAllBatch(Iterable<ID> ids);


}
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




    @Transactional
    public void flush();

    /**
     * 更新指定字段：先find再更新 (Find-then-Update)
     */
    @Transactional
    public void updateField(T entity, List<String> fieldsToUpdate);


    /**
     * 直接更新指定字段：使用 CriteriaUpdate (Direct Update)
     */
    @Transactional
    public void updateFieldDirect(T entity, List<String> fieldsToUpdate);



    T findOne(ID id);

    public List<T> findAllById(ID[] ids);


    public long count();

    /**
     * 将实体刷新，避免从缓存取
     */
    public void refresh(T t);

    public T findByIdAndRefresh(ID id);


    // --- 5.1 JpaQuery/字段等值查询 (Custom Query Helpers) ---

    public T findByField(String key, Object value);

    public T findByField(String key, Object value, String key2, Object value2);

    public T findByField(String key, Object value, String key2, Object value2, String key3, Object value3);


    public List<T> findAllByField(String key, Object value);

    public default Spec<T> spec() {
        return Spec.of();
    }

    public List<T> findAllByField(String key, Object value, String key2, Object value2);

    /**
     * 判断字段值是否存在
     * 例如修改用户名时，判断用户名是否唯一
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    public boolean isFieldExist(ID id, String fieldName, Object value);


    /**
     * 判断字段值是否唯一
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    public boolean isUnique(ID id, String fieldName, Object value);

    public List<T> findByExampleLike(T t, Sort sort);

    public Page<T> findByExampleLike(T t, Pageable pageable);

    public T findTop1(Specification<T> c, Sort sort);


    /**
     * 查询前几
     */
    public List<T> findTop(int size, Specification<T> c, Sort sort);

    /***
     * 查询字段列表
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

    public Map<ID, T> findKeyed(Iterable<ID> ids);


    /**
     * 将查找接口转换为map， key为id，value为对象
     */
    public Map<ID, T> dict();
    public Map<ID, T> dict(Specification<T> spec);

    public Map<ID, T> dict(Specification<T> spec, Function<T, ID> keyField);

    /**
     * 将查询结果的两个字段组装成map
     */
    public <V> Map<ID, V> dict(Specification<T> spec, Function<T, ID> keyField, Function<T, V> valueField);

    /**
     * 获取实体的ID
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
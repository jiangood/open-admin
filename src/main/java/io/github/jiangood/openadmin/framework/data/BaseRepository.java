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




    public List<T> findAllById(String[] ids);


    public long count();

    /**
     * 将实体刷新，避免从缓存取
     */
    public void refresh(T t);

    public T findByIdAndRefresh(String id);

    T findByIdOrNull(ID id);


    // --- 5.1 JpaQuery/字段等值查询 (Custom Query Helpers) ---

    public T findByField(String key, Object value);

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
    public boolean isFieldExist(String id, String fieldName, Object value);


    /**
     * 判断字段值是否唯一
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    public boolean isUnique(String id, String fieldName, Object value);

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
     * 例子
     * Spec<User> spec = Spec.<User>of()
     * .select("username")
     * .selectFnc(Spec.Fuc.SUM, "age")
     * .selectFnc(Spec.Fuc.COUNT, "age").
     * groupBy("username");
     *
     * @param spec
     * @return 列表，
     */
    public List<Dict> stats(Specification<T> spec);

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


}
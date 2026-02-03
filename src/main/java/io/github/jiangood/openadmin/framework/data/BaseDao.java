package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 基础dao
 */
public abstract class BaseDao<T extends Persistable<String>> {

    // --- 1. 字段和资源注入 ---

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    protected JpaEntityInformation<T, ?> entityInformation;

    @Getter
    protected Class<T> domainClass;
    private SimpleJpaRepository<T, String> rep;

    @Resource
    private Validator validator;

    // --- 2. 生命周期与初始化 ---

    @PostConstruct
    private void init() {
        this.domainClass = parseDomainClass();
        this.entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
        this.rep = new SimpleJpaRepository<>(domainClass, entityManager);
    }

    // --- 3. 核心写入操作 (CRUD - Create, Update, Delete) ---

    /**
     * 插入或更新
     *
     * @param entity
     * @return
     */
    @Transactional
    public T save(T entity) {
        return rep.save(entity);
    }


    /**
     * 批量新增/更新 (委托给 SimpleJpaRepository 的 saveAll)
     */
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        return rep.saveAll(entities);
    }

    @Transactional
    public T saveAndFlush(T entity) {
        return rep.saveAndFlush(entity);
    }

    /**
     * 批量新增/更新并立即同步
     */
    @Transactional
    public List<T> saveAllAndFlush(Iterable<T> entities) {
        return rep.saveAllAndFlush(entities);
    }

    @Transactional
    public void flush() {
        entityManager.flush();
    }

    /**
     * 更新指定字段：先find再更新 (Find-then-Update)
     */
    @Transactional
    public void updateField(T entity, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");
        String id = entity.getId();
        Assert.hasText(id, "id不能为空");

        T db = findById(id);
        Assert.notNull(db, "数据不存在");

        for (String fieldName : fieldsToUpdate) {
            Object fieldValue = BeanUtil.getFieldValue(entity, fieldName);
            BeanUtil.setFieldValue(db, fieldName, fieldValue);
        }
    }


    /**
     * 直接更新指定字段：使用 CriteriaUpdate (Direct Update)
     */
    @Transactional
    public void updateFieldDirect(T entity, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");
        String id = entity.getId();
        Assert.hasText(id, "id不能为空");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class<T> cls = getDomainClass();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(cls);
        Root<T> root = update.from(cls);

        for (String fieldName : fieldsToUpdate) {
            Object value = BeanUtil.getFieldValue(entity, fieldName);
            // 校验
            Set<ConstraintViolation<T>> entityViolations = validator.validateValue(cls, fieldName, value);
            if (!entityViolations.isEmpty()) {
                throw new ConstraintViolationException(entityViolations);
            }

            update.set(root.get(fieldName), value);
        }
        update.where(cb.equal(root.get("id"), id));

        // 执行更新
        int updated = entityManager.createQuery(update).executeUpdate();
        if (updated == 0) {
            throw new OptimisticLockingFailureException("更新失败，记录可能已被删除");
        }
    }


    @Transactional
    public void delete(T entity) {
        rep.delete(entity);
    }

    @Transactional
    public void deleteById(String id) {
        rep.deleteById(id);
    }

    @Transactional
    public void deleteAllById(Iterable<String> ids) {
        rep.deleteAllById(ids);
    }

    @Transactional
    public void deleteAllById(String[] ids) {
        rep.deleteAllById(List.of(ids));
    }

    @Transactional
    public void deleteAll(Iterable<T> entities) {
        rep.deleteAll(entities);
    }

    @Transactional
    public void deleteAllInBatch() {
        rep.deleteAllInBatch();
    }

    @Transactional
    public void deleteAll() {
        rep.deleteAll();
    }

    @Transactional
    public void deleteAllInBatch(Iterable<T> entities) {
        rep.deleteAllInBatch(entities);
    }

    @Transactional
    public void deleteAllByIdInBatch(Iterable<String> ids) {
        rep.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public long delete(Specification<T> spec) {
        return rep.delete(spec);
    }


    // --- 4. 核心读取操作 (CRUD - Read) ---

    public T findById(String id) {
        Assert.hasText(id, "id不能为空");
        return rep.findById(id).orElse(null);
    }

    @Deprecated
    public T findOne(String id) {
        return rep.findById(id).orElse(null);
    }

    @Deprecated
    public T findOne(T t) {
        return rep.findById(t.getId()).orElse(null);
    }

    public boolean existsById(String id) {
        return rep.existsById(id);
    }

    public List<T> findAll() {
        return rep.findAll();
    }

    public List<T> findAll(Sort sort) {
        return rep.findAll(sort);
    }


    public List<T> findAllById(Iterable<String> ids) {
        return rep.findAllById(ids);
    }

    public List<T> findAllById(String[] ids) {
        return rep.findAllById(List.of(ids));
    }

    public Page<T> findAll(Pageable pageable) {
        return rep.findAll(pageable);
    }

    public long count() {
        return rep.count();
    }

    /**
     * 将实体刷新，避免从缓存取
     */
    public void refresh(T t) {
        if (t != null) {
            entityManager.refresh(t);
        }
    }

    public T findByIdAndRefresh(String id) {
        T t = this.findById(id);
        this.refresh(t);
        return t;
    }

    // --- 5. 定制化查询 (Specification/Example/JpaQuery) ---

    public T findOne(Specification<T> spec) {
        return rep.findOne(spec).orElse(null);
    }

    // 补充：SimpleJpaRepository 的 findOne(Example)
    public <S extends T> T findOne(Example<S> example) {
        return rep.findOne(example).orElse(null);
    }

    public List<T> findAll(Specification<T> spec) {
        return rep.findAll(spec);
    }

    public List<T> findAll(Specification<T> spec, Sort sort) {
        return rep.findAll(spec, sort);
    }

    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return rep.findAll(spec, pageable);
    }

    // 补充：SimpleJpaRepository 的 findAll(Example)
    public <S extends T> List<S> findAll(Example<S> example) {
        return rep.findAll(example);
    }

    // 补充：SimpleJpaRepository 的 findAll(Example, Sort)
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return rep.findAll(example, sort);
    }

    // 补充：SimpleJpaRepository 的 findAll(Example, Pageable)
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return rep.findAll(example, pageable);
    }

    public boolean exists(Specification<T> spec) {
        return rep.exists(spec);
    }

    // 补充：SimpleJpaRepository 的 exists(Example)
    public <S extends T> boolean exists(Example<S> example) {
        return rep.exists(example);
    }

    public long count(Specification<T> spec) {
        return rep.count(spec);
    }

    // 补充：SimpleJpaRepository 的 count(Example)
    public <S extends T> long count(Example<S> example) {
        return rep.count(example);
    }

    public <R> R findBy(Specification<T> spec, Function<FluentQuery.FetchableFluentQuery<T>, R> queryFunction) {
        return rep.findBy(spec, queryFunction);
    }


    // --- 5.1 JpaQuery/字段等值查询 (Custom Query Helpers) ---

    public T findByField(String key, Object value) {
        return this.findOne(Spec.<T>of().eq(key, value));
    }

    public T findByField(String key, Object value, String key2, Object value2) {
        return this.findOne(Spec.<T>of().eq(key, value).eq(key2, value2));
    }

    public T findByField(String key, Object value, String key2, Object value2, String key3, Object value3) {
        return this.findOne(Spec.<T>of().eq(key, value).eq(key2, value2).eq(key3, value3));
    }


    public List<T> findAllByField(String key, Object value) {
        return this.findAll(spec().eq(key, value));
    }

    public Spec<T> spec() {
        return Spec.of();
    }

    public List<T> findAllByField(String key, Object value, String key2, Object value2) {
        return this.findAll(Spec.<T>of().eq(key, value).eq(key2, value2));
    }

    /**
     * 判断字段值是否存在
     * 例如修改用户名时，判断用户名是否唯一
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    public boolean isFieldExist(String id, String fieldName, Object value) {
        Spec<T> spec = Spec.of();

        spec.ne("id", id); // 修改数据时，不包含自身

        spec.eq(fieldName, value);
        return rep.exists(spec);
    }

    public List<T> findByExampleLike(T t, Sort sort) {
        return this.rep.findAll(Spec.<T>of().addExample(t), sort);
    }

    public Page<T> findByExampleLike(T t, Pageable pageable) {
        return this.rep.findAll(Spec.<T>of().addExample(t), pageable);
    }

    public T findTop1(Specification<T> c, Sort sort) {
        List<T> result = this.findTop(1, c, sort);
        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * 查询前几
     */
    public List<T> findTop(int size, Specification<T> c, Sort sort) {
        Page<T> page = this.findAll(c, PageRequest.of(0, size, sort));
        return page.hasContent() ? page.getContent() : Collections.emptyList();
    }

    /***
     * 查询字段列表
     */
    public <R> List<R> findField(String fieldName, Specification<T> c) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery();
        Root root = query.from(domainClass);

        Path path = root.get(fieldName);
        Predicate predicate = c.toPredicate(root, query, builder);

        query.select(path).where(predicate);

        return (List<R>) entityManager.createQuery(query).getResultList();
    }


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
    public List<Dict> stats(Specification<T> spec) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
        Root<T> root = query.from(domainClass);

        Predicate predicate = spec.toPredicate(root, query, builder);
        query.where(predicate);


        Selection<?> selection = query.getSelection();
        if (selection == null) {
            throw new IllegalArgumentException("请指定查询字段");
        }
        List<Selection<?>> selections = selection.getCompoundSelectionItems();
        List<Object[]> resultList = entityManager.createQuery(query).getResultList();

        // 转换为map
        return resultList.stream().map(record -> {
            Dict map = Dict.create();
            for (int i = 0; i < selections.size(); i++) {
                Selection<?> item = selections.get(i);
                map.put(item.getAlias(), record[i]);
            }
            return map;
        }).toList();
    }

    public Dict statsSingleResult(Specification<T> spec) {
        List<Dict> list = stats(spec);
        Assert.state(list.size() == 1, "结果个数错误，期望1个，实际" + list.size() + "个");
        return list.get(0);
    }


    // --- 7. 结果集映射 (Dictionary Mapping) ---

    public Map<String, T> findKeyed(Iterable<String> ids) {
        List<T> list = this.findAllById(ids);
        Map<String, T> map = new HashMap<>();
        for (T t : list) {
            map.put(t.getId(), t);
        }
        return map;
    }

    /**
     * 将查找接口转换为map， key为id，value为对象
     */
    public Map<String, T> dict(Specification<T> spec) {
        List<T> list = this.findAll(spec);
        Map<String, T> map = new HashMap<>();
        for (T t : list) {
            map.put(t.getId(), t);
        }
        return map;
    }

    public Map<String, T> dict(Specification<T> spec, Function<T, String> keyField) {
        List<T> list = this.findAll(spec);
        Map<String, T> map = new HashMap<>();
        for (T t : list) {
            String key = keyField.apply(t);
            if (key != null) {
                map.put(key, t);
            }
        }
        return map;
    }

    /**
     * 将查询结果的两个字段组装成map
     */
    public <V> Map<String, V> dict(Specification<T> spec, Function<T, String> keyField, Function<T, V> valueField) {
        List<T> list = this.findAll(spec);
        Map<String, V> map = new HashMap<>();
        for (T t : list) {
            String key = keyField.apply(t);
            if (key != null) {
                map.put(key, valueField.apply(t));
            }
        }
        return map;
    }


    // --- 8. 工具与帮助方法 ---

    /**
     * 解析泛型参数获取领域实体类
     */
    private Class<T> parseDomainClass() {
        Type type = getClass().getGenericSuperclass();

        //解决多层继承拿泛型类型   //BaseBaseService<User> <- UserService <- PassportService
        while (!(type instanceof ParameterizedType)) {
            type = ((Class<?>) type).getGenericSuperclass();
            if (type == null || "java.lang.Object".equals(type.getClass().getName())) {
                break;
            }
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Type[] genericTypes = parameterizedType.getActualTypeArguments();
            return (Class<T>) genericTypes[0];
        }
        throw new IllegalStateException("解析DomainClass失败");
    }

}

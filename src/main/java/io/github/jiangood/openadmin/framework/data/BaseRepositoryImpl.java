package io.github.jiangood.openadmin.framework.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation,
                              EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

    @Override
    public T findOne(ID id) {
        return super.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void flush() {
        entityManager.flush();
    }

    /**
     * 更新指定字段：先find再更新 (Find-then-Update)
     */
    @Transactional
    @Override
    public void updateField(T entity, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");
        ID id = getId(entity);
        Assert.notNull(id, "id不能为空");

        T db = findById(id).orElse(null);
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
    @Override
    public void updateFieldDirect(T entity, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");
        Assert.notNull(entity, "实体对象不能为空");

        ID id = getId(entity);
        Assert.notNull(id, "实体ID不能为空");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(domainClass);
        Root<T> root = update.from(domainClass);

        for (String fieldName : fieldsToUpdate) {
            Assert.hasText(fieldName, "字段名不能为空");
            Object value = BeanUtil.getFieldValue(entity, fieldName);
            update.set(root.get(fieldName), value);
        }
        update.where(cb.equal(root.get("id"), id));

        // 执行更新
        int updated = entityManager.createQuery(update).executeUpdate();
        if (updated == 0) {
            throw new IllegalArgumentException(String.format("更新失败，记录可能已被删除或ID不存在: %s", id));
        }
    }


    @Override
    public List<T> findAllById(ID[] ids) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        List<ID> idList = new ArrayList<>();
        for (ID id : ids) {
            if (id != null) {
                idList.add(id);
            }
        }

        if (idList.isEmpty()) {
            return Collections.emptyList();
        }

        return findAllById(idList);
    }

    @Override
    public long count() {
        return super.count();
    }

    /**
     * 将实体刷新，避免从缓存取
     */
    @Override
    public void refresh(T t) {
        if (t != null) {
            // 检查实体是否被管理
            if (entityManager.contains(t)) {
                entityManager.refresh(t);
            }
        }
    }

    @Transactional
    @Override
    public T findByIdAndRefresh(ID id) {
        T t = findById(id).orElse(null);
        refresh(t);
        return t;
    }


    // --- 5.1 JpaQuery/字段等值查询 (Custom Query Helpers) ---

    @Override
    public T findByField(String key, Object value) {
        return findOne(Spec.<T>of().eq(key, value)).orElse(null);
    }

    @Override
    public T findByField(String key, Object value, String key2, Object value2) {
        return findOne(Spec.<T>of().eq(key, value).eq(key2, value2)).orElse(null);
    }

    @Override
    public T findByField(String key, Object value, String key2, Object value2, String key3, Object value3) {
        return findOne(Spec.<T>of().eq(key, value).eq(key2, value2).eq(key3, value3)).orElse(null);
    }


    @Override
    public List<T> findAllByField(String key, Object value) {
        return findAll(Spec.<T>of().eq(key, value));
    }

    @Override
    public List<T> findAllByField(String key, Object value, String key2, Object value2) {
        return findAll(Spec.<T>of().eq(key, value).eq(key2, value2));
    }

    /**
     * 判断字段值是否存在
     * 例如修改用户名时，判断用户名是否唯一
     *
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    @Override
    public boolean isFieldExist(ID id, String fieldName, Object value) {
        Specification<T> spec = Spec.<T>of().ne("id", id).eq(fieldName, value);
        return exists(spec);
    }


    /**
     * 判断字段值是否唯一
     *
     * @param id
     * @param fieldName
     * @param value
     * @return
     */
    @Override
    public boolean isUnique(ID id, String fieldName, Object value) {
        boolean exist = isFieldExist(id, fieldName, value);
        return !exist;
    }

    @Override
    public List<T> findByExampleLike(T t, Sort sort) {
        return findAll(Spec.<T>of().addExample(t), sort);
    }

    @Override
    public Page<T> findByExampleLike(T t, Pageable pageable) {
        return findAll(Spec.<T>of().addExample(t), pageable);
    }

    @Override
    public T findTop1(Specification<T> c, Sort sort) {
        List<T> result = findTop(1, c, sort);
        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * 查询前几
     */
    @Override
    public List<T> findTop(int size, Specification<T> c, Sort sort) {
        Page<T> page = findAll(c, PageRequest.of(0, size, sort));
        return page.hasContent() ? page.getContent() : Collections.emptyList();
    }

    /***
     * 查询字段列表
     */
    @Override
    public <R> List<R> findField(String fieldName, Specification<T> c) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = builder.createQuery();
        Root<T> root = query.from(domainClass);

        Path<?> path = root.get(fieldName);
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
    @Override
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

    @Override
    public Dict statsSingleResult(Specification<T> spec) {
        List<Dict> list = stats(spec);
        Assert.state(list.size() == 1, "结果个数错误，期望1个，实际" + list.size() + "个");
        return list.get(0);
    }


    // --- 7. 结果集映射 (Dictionary Mapping) ---

    @Override
    public Map<ID, T> findKeyed(Iterable<ID> ids) {
        List<ID> idList = new ArrayList<>();
        for (ID id : ids) {
            idList.add((ID) id);
        }
        List<T> list = findAllById(idList);
        Map<ID, T> map = new HashMap<>();
        for (T t : list) {
            if (t instanceof Persistable) {
                map.put(getId(t), t);
            }
        }
        return map;
    }

    @Override
    public Map<ID, T> dict() {
        return dict(null);
    }

    /**
     * 将查找接口转换为map， key为id，value为对象
     */
    @Override
    public Map<ID, T> dict(Specification<T> spec) {
        List<T> list = findAll(spec);
        Map<ID, T> map = new HashMap<>();
        for (T t : list) {
            if (t instanceof Persistable) {
                map.put(getId(t), t);
            }
        }
        return map;
    }

    @Override
    public Map<ID, T> dict(Specification<T> spec, Function<T, ID> keyField) {
        List<T> list = findAll(spec);
        Map<ID, T> map = new HashMap<>();
        for (T t : list) {
            ID key = keyField.apply(t);
            if (key != null) {
                map.put(key, t);
            }
        }
        return map;
    }

    /**
     * 将查询结果的两个字段组装成map
     */
    @Override
    public <V> Map<ID, V> dict(Specification<T> spec, Function<T, ID> keyField, Function<T, V> valueField) {
        List<T> list = findAll(spec);
        Map<ID, V> map = new HashMap<>();
        for (T t : list) {
            ID key = keyField.apply(t);
            if (key != null) {
                map.put(key, valueField.apply(t));
            }
        }
        return map;
    }

    /**
     * 获取实体的ID
     */
    @SuppressWarnings("unchecked")
    @Override
    public ID getId(T entity) {
        return (ID) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil().getIdentifier(entity);
    }

    // --- 8. Batch Operations --- 

    /**
     * 批量保存实体
     */
    @Transactional
    @Override
    public List<T> saveAllBatch(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        int count = 0;
        for (T entity : entities) {
            entityManager.persist(entity);
            result.add(entity);

            // 每100个实体刷新一次，避免内存占用过高
            if (++count % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        return result;
    }

    /**
     * 批量更新指定字段
     */
    @Transactional
    @Override
    public void updateFieldBatch(Iterable<T> entities, List<String> fieldsToUpdate) {
        Assert.notEmpty(fieldsToUpdate, "fieldsToUpdate不能为空");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        int count = 0;

        for (T entity : entities) {
            ID id = getId(entity);
            Assert.notNull(id, "id不能为空");

            CriteriaUpdate<T> update = cb.createCriteriaUpdate(domainClass);
            Root<T> root = update.from(domainClass);

            for (String fieldName : fieldsToUpdate) {
                Object value = cn.hutool.core.bean.BeanUtil.getFieldValue(entity, fieldName);
                update.set(root.get(fieldName), value);
            }
            update.where(cb.equal(root.get("id"), id));

            entityManager.createQuery(update).executeUpdate();

            // 每100个实体刷新一次
            if (++count % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
    }

    /**
     * 批量删除
     */
    @Transactional
    @Override
    public void deleteAllBatch(Iterable<ID> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(domainClass);
        Root<T> root = delete.from(domainClass);

        List<ID> idList = new ArrayList<>();
        for (ID id : ids) {
            idList.add(id);
        }

        if (!idList.isEmpty()) {
            delete.where(root.get("id").in(idList));
            entityManager.createQuery(delete).executeUpdate();
        }
    }

}
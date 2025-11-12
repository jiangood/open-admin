package io.admin.framework.data.repository;

import cn.hutool.core.bean.BeanUtil;
import io.admin.framework.data.query.ExpressionTool;
import io.admin.framework.data.query.JpaQuery;
import io.admin.framework.data.query.StatField;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * 基础dao
 * BaseDao的查询条件不依赖JpaQuery
 */
public class BaseDao<T extends Persistable<String>> {

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    protected JpaEntityInformation<T, ?> entityInformation;

    @Getter
    protected Class<T> domainClass;
    private SimpleJpaRepository<T, String> rep;

    @Resource
    private Validator validator;

    @PostConstruct
    private void init() {
        this.domainClass = parseDomainClass();
        this.entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
        this.rep = new SimpleJpaRepository<>(domainClass, entityManager);
    }


    @Transactional
    public void deleteById(String id) {
        rep.deleteById(id);
    }

    public boolean isFieldUnique(String id, String fieldName, Object value) {
        JpaQuery<T> q = new JpaQuery<>();
        q.ne("id", id);
        q.eq(fieldName, value);
        return rep.exists(q);
    }


    @Transactional
    public void delete(T entity) {
        rep.delete(entity);
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
    public void deleteAllByIdInBatch(Iterable<String> ids) {
        rep.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public void deleteAll(Iterable<T> entities) {
        rep.deleteAll(entities);
    }

    @Transactional
    public void deleteAllInBatch(Iterable<T> entities) {
        rep.deleteAllInBatch(entities);
    }

    @Transactional
    public void deleteAll() {
        rep.deleteAll();
    }

    @Transactional
    public void deleteAllInBatch() {
        rep.deleteAllInBatch();
    }

    public T findById(String id) {
        return rep.findById(id).orElse(null);
    }

    public T findByIdAndRefresh(String id) {
        T t = this.findById(id);
        this.refresh(t);
        return t;
    }

    public T findByField(String key, Object value) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        return this.findOne(q);
    }

    public T findByField(String key, Object value, String key2, Object value2) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        q.eq(key2, value2);
        return this.findOne(q);
    }

    public List<T> findAllByField(String key, Object value) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        return this.findAll(q);
    }

    public List<T> findAllByField(String key, Object value, String key2, Object value2) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        q.eq(key2, value2);
        return this.findAll(q);
    }

    /**
     * 将实体刷新，避免从缓存取
     */
    public void refresh(T t) {
        if (t != null) {
            entityManager.refresh(t);
        }
    }

    public boolean existsById(String id) {
        return rep.existsById(id);
    }

    public List<T> findAll() {
        return rep.findAll();
    }

    public List<T> findAllById(Iterable<String> ids) {
        return rep.findAllById(ids);
    }

    public List<T> findAllById(String[] ids) {
        return rep.findAllById(List.of(ids));
    }

    public List<T> findAll(Sort sort) {
        return rep.findAll(sort);
    }

    public Page<T> findAll(Pageable pageable) {
        return rep.findAll(pageable);
    }


    public List<T> findAll(Specification<T> spec) {
        return rep.findAll(spec);
    }

    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return rep.findAll(spec, pageable);
    }

    public List<T> findAll(Specification<T> spec, Sort sort) {
        return rep.findAll(spec, sort);
    }

    public boolean exists(Specification<T> spec) {
        return rep.exists(spec);
    }

    @Transactional
    public long delete(Specification<T> spec) {
        return rep.delete(spec);
    }

    public <R> R findBy(Specification<T> spec, Function<FluentQuery.FetchableFluentQuery<T>, R> queryFunction) {
        return rep.findBy(spec, queryFunction);
    }

    public T findOne(Example<T> example) {
        return rep.findOne(example).orElse(null);
    }

    public T findOne(Specification<T> spec) {
        return rep.findOne(spec).orElse(null);
    }

    public T findOne(T t) {
        return rep.findById(t.getId()).orElse(null);
    }

    public T findOne(String key, Object value) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        return this.findOne(q);
    }

    public T findOne(String key, Object value, String key2, Object value2) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        q.eq(key2, value2);
        return this.findOne(q);
    }

    public T findOne(String key, Object value, String key2, Object value2, String key3, Object value3) {
        JpaQuery<T> q = new JpaQuery<>();
        q.eq(key, value);
        q.eq(key2, value2);
        q.eq(key3, value3);
        return this.findOne(q);
    }

    public long count(Example<T> example) {
        return rep.count(example);

    }

    public boolean exists(Example<T> example) {
        return rep.exists(example);
    }

    public List<T> findAll(Example<T> example) {
        return rep.findAll(example);
    }

    public List<T> findAll(Example<T> example, Sort sort) {
        return rep.findAll(example, sort);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return rep.findAll(example, pageable);
    }


    public long count() {
        return rep.count();

    }

    public long count(Specification<T> spec) {
        return rep.count(spec);
    }

    /**
     * 插入或更新
     *
     * @param entity
     * @return
     */
    @Transactional
    public T save(T entity) {
        Assert.notNull(entity, "Entity must not be null");

        // 新增
        if (this.entityInformation.isNew(entity)) {
            this.entityManager.persist(entity);
            return entity;
        }

        return this.entityManager.merge(entity);
    }

    @Transactional
    public T saveAndFlush(T entity) {
        T result = save(entity);
        flush();

        return result;
    }

    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    @Transactional
    public List<T> saveAllAndFlush(Iterable<T> entities) {
        List<T> result = saveAll(entities);
        flush();

        return result;
    }


    @Transactional
    public T merge(T entity) {
        return entityManager.merge(entity);
    }

    /**
     * 更新指定字段
     * 对比save方法更新的时所有字段，只更新指定字段
     *
     * @gendoc
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
     * 直接更新指定字段
     * 不会先find，再更新
     * 对比save方法更新的时所有字段，改方法只更新指定字段
     * 注意：主要用于更新单个实体的字段， 不能更新多对多等关联关系
     *
     * @gendoc
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


    /**
     * 保存数据
     * 和save的区别是不再判断实体是否存在
     *
     * @param entity
     * @return
     */
    @Transactional
    public T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }


    @Transactional
    public void flush() {
        entityManager.flush();
    }


    public T findOne(String id) {
        return rep.findById(id).orElse(null);
    }

    public Map<String, T> findKeyed(Iterable<String> ids) {
        List<T> list = this.findAllById(ids);
        Map<String, T> map = new HashMap<>();
        for (T t : list) {
            map.put(t.getId(), t);
        }
        return map;
    }


    public T findTop1(Specification<T> c, Sort sort) {
        PageRequest pageRequest = PageRequest.of(0, 1, sort);

        Page<T> all = this.findAll(c, pageRequest);
        if (all.getTotalElements() > 0) {
            return all.getContent().get(0);
        }
        return null;
    }


    public List<T> findTop(int topSize, Specification<T> c, Sort sort) {
        PageRequest pageRequest = PageRequest.of(0, topSize, sort);

        Page<T> all = this.findAll(c, pageRequest);
        if (all.getTotalElements() > 0) {
            return all.getContent();
        }
        return Collections.emptyList();
    }

    /***
     * 查询字段列表
     * 如姓名列表
     * @param fieldName
     * @param c
     * @return
     * @param <R>
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

    /**
     * 分组统计
     *
     * @return
     */
    public List<Map> groupStats(Specification<T> spec, String[] groupFields, StatField... statFields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Map> query = builder.createQuery(Map.class);
        Root<T> root = query.from(domainClass);
        List<Selection<?>> selections = new ArrayList<>();


        Expression[] groups = new Expression[groupFields.length];
        for (int i = 0; i < groupFields.length; i++) {
            String groupField = groupFields[i];
            Expression group = ExpressionTool.getExpression(groupField, root); //分组字段
            groups[i] = group;
            group.alias(groupField);
            selections.add(group);
        }

        for (StatField statField : statFields) {
            String fieldName = statField.getName();
            Path<Number> f = root.get(fieldName);
            Expression<?> statExpr = null;
            switch (statField.getType()) {
                case SUM:
                    statExpr = builder.sum(f);
                    break;
                case COUNT:
                    statExpr = builder.count(f);
                    break;
                case AVG:
                    statExpr = builder.avg(f);
                    break;
                case MIN:
                    statExpr = (builder.min(f));
                    break;
                case MAX:
                    statExpr = (builder.max(f));
                    break;
                default:
                    throw new IllegalStateException("not support stat type " + statField.getType());

            }
            selections.add(statExpr.alias(fieldName));
        }

        Predicate predicate = spec.toPredicate(root, query, builder);
        query.multiselect(selections).where(predicate).groupBy(groups);

        return entityManager.createQuery(query).getResultList();
    }

    public List<Map> groupStats(Specification<T> spec, String groupFields, StatField... statFields) {
        return this.groupStats(spec, new String[]{groupFields}, statFields);
    }

    public Map stats(Specification<T> spec, StatField... statFields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Map> query = builder.createQuery(Map.class);
        Root<T> root = query.from(domainClass);
        List<Selection<?>> selections = new ArrayList<>();


        for (StatField statField : statFields) {
            String fieldName = statField.getName();
            Path<Number> f = root.get(fieldName);
            Expression<?> statExpr = null;
            switch (statField.getType()) {
                case SUM:
                    statExpr = builder.sum(f);
                    break;
                case COUNT:
                    statExpr = builder.count(f);
                    break;
                case AVG:
                    statExpr = builder.avg(f);
                    break;
                case MIN:
                    statExpr = (builder.min(f));
                    break;
                case MAX:
                    statExpr = (builder.max(f));
                    break;
                default:
                    throw new IllegalStateException("not support stat type " + statField.getType());

            }
            selections.add(statExpr.alias(fieldName));
        }

        Predicate predicate = spec.toPredicate(root, query, builder);
        query.multiselect(selections).where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    /**
     * 分株统计数量
     *
     * @param q
     * @param groupField
     * @return
     */
    public Map<String, Long> count(Specification<T> q, String groupField) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = builder.createQuery(Object.class);
        Root<T> root = query.from(domainClass);

        Expression group = ExpressionTool.getExpression(groupField, root); // 支持 . 分割， 如 user.id

        Predicate predicate = q.toPredicate(root, query, builder);
        query.multiselect(group, builder.count(root)).where(predicate).groupBy(group);

        List<Object> resultList = entityManager.createQuery(query).getResultList();

        // 组装数据结构
        Map<String, Long> map = new HashMap<>();
        for (Object row : resultList) {
            Object[] rowArr = (Object[]) row;
            String groupValue = (String) rowArr[0];
            Number count = (Number) rowArr[1];
            if (count != null) {
                map.put(groupValue, count.longValue());
            }
        }
        return map;
    }

    /**
     * 将查找接口转换为map， key为id，value为对象
     *
     * @param spec
     * @return
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
     *
     * @param spec
     * @param keyField
     * @param valueField
     * @param <V>
     * @return
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

    public List<T> findByExampleLike(T t, Sort sort) {
        JpaQuery<T> c = new JpaQuery<>();
        c.likeExample(t);
        return this.rep.findAll(c, sort);
    }


    public Page<T> findByExampleLike(T example, Pageable pageable) {
        JpaQuery<T> query = new JpaQuery<>();
        query.likeExample(example);
        return this.rep.findAll(query, pageable);
    }

    private Class<T> parseDomainClass() {
        Type type = getClass().getGenericSuperclass();

        //解决多层继承拿泛型类型   //BaseBaseService<User> <- UserService <- PassportService
        while (!(type instanceof ParameterizedType)) {
            type = ((Class<?>) type).getGenericSuperclass();
            if (type == null || "java.lang.Object".equals(type.getClass().getName())) {
                break;
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] genericTypes = parameterizedType.getActualTypeArguments();
            return (Class<T>) genericTypes[0];
        }
        throw new IllegalStateException("解析DomainClass失败");
    }


}

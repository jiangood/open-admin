package io.github.jiangood.openadmin.framework.data.impl;

import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class JdbcRunnerImpl implements JdbcRunner {

    private final JdbcTemplate jdbc;

    public JdbcRunnerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    // ---- 查询 - 单条 ----

    @Override
    public <T> T findById(String table, Object id, Class<T> cls) {
        return findOne(cls, "select * from " + validateIdentifier(table) + " where id=?", id);
    }

    @Override
    public Map<String, Object> findById(String table, Object id) {
        return findOne("select * from " + validateIdentifier(table) + " where id=?", id);
    }

    @Override
    public <T> T findOne(Class<T> cls, String sql, Object... params) {
        return jdbc.query(sql, rs -> rs.next() ? mapToBean(rs, cls) : null, params);
    }

    @Override
    public Map<String, Object> findOne(String sql, Object... params) {
        return jdbc.query(sql, rs -> rs.next() ? mapToMap(rs) : null, params);
    }

    // ---- 查询 - 列表 ----

    @Override
    public <T> List<T> findAll(String table, Class<T> cls) {
        return findAll(cls, "select * from " + validateIdentifier(table));
    }

    @Override
    public <T> List<T> findAll(Class<T> cls, String sql, Object... params) {
        return jdbc.query(sql, (rs, rowNum) -> mapToBean(rs, cls), params);
    }

    @Override
    public List<Map<String, Object>> findAll(String sql, Object... params) {
        return jdbc.query(sql, (rs, rowNum) -> mapToMap(rs), params);
    }

    // ---- 查询 - 分页 ----

    @Override
    public <T> Page<T> findAll(String table, Pageable pageable, Class<T> cls) {
        return findAll(cls, pageable, "select * from " + validateIdentifier(table));
    }

    @Override
    public <T> Page<T> findAll(Class<T> cls, Pageable pageable, String sql, Object... params) {
        String pageSql = applyPagination(sql, pageable);
        return pageQuery(params, "select count(*) from (" + sql + ") t",
                pageable, p -> findAll(cls, pageSql, p));
    }

    @Override
    public Page<Map<String, Object>> findAll(Pageable pageable, String sql, Object... params) {
        String pageSql = applyPagination(sql, pageable);
        return pageQuery(params, "select count(*) from (" + sql + ") t",
                pageable, p -> findAll(pageSql, p));
    }

    private String applyPagination(String sql, Pageable pageable) {
        String orderBy = toOrderBy(pageable.getSort());
        if (!orderBy.isEmpty()) {
            sql = sql + " " + orderBy;
        }
        return dialect().apply(sql, pageable.getPageSize(), pageable.getOffset());
    }

    private static String toOrderBy(Sort sort) {
        if (sort.isEmpty()) {
            return "";
        }
        StringJoiner orderBy = new StringJoiner(",", "order by ", "");
        for (Sort.Order order : sort) {
            orderBy.add(validateIdentifier(order.getProperty()) + " " + (order.isDescending() ? "desc" : "asc"));
        }
        return orderBy.toString();
    }

    private <T> Page<T> pageQuery(Object[] params, String countSql, Pageable pageable, Function<Object[], List<T>> queryFn) {
        long count = findLong(countSql, params);
        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return new PageImpl<>(queryFn.apply(params), pageable, count);
    }

    // ---- 标量与存在性 ----

    @Override
    public long count(String table) {
        Long count = findLong("select count(*) from " + validateIdentifier(table));
        return count != null ? count : 0;
    }

    @Override
    public Long findLong(String sql, Object... params) {
        Object result = jdbc.query(sql, rs -> rs.next() ? rs.getObject(1) : null, params);
        return switch (result) {
            case Long l -> l;
            case Integer i -> i.longValue();
            case Number n -> n.longValue();
            case null -> null;
            default -> Long.parseLong(result.toString());
        };
    }

    @Override
    public boolean existsById(String table, Object id) {
        Long count = findLong("select count(*) from " + validateIdentifier(table) + " where id=?", id);
        return count != null && count > 0;
    }

    // ---- 分页方言（按数据库产品名自动探测并缓存） ----

    private enum Dialect {
        LIMIT_OFFSET("limit {size} offset {offset}"),
        OFFSET_FETCH("offset {offset} rows fetch next {size} rows only") {
            @Override
            String apply(String sql, long size, long offset) {
                // SQL Server / Oracle 的 OFFSET FETCH 语法要求 ORDER BY，缺省时按首列排序兜底
                if (!sql.toLowerCase(Locale.ROOT).contains("order by")) {
                    sql += " order by 1";
                }
                return super.apply(sql, size, offset);
            }
        };

        private final String paginationSql;

        Dialect(String paginationSql) {
            this.paginationSql = paginationSql;
        }

        String apply(String sql, long size, long offset) {
            return sql + " " + paginationSql
                    .replace("{size}", String.valueOf(size))
                    .replace("{offset}", String.valueOf(offset));
        }
    }

    private volatile Dialect dialect;

    private Dialect dialect() {
        if (dialect == null) {
            synchronized (this) {
                if (dialect == null) {
                    dialect = detectDialect();
                }
            }
        }
        return dialect;
    }

    private Dialect detectDialect() {
        DataSource dataSource = jdbc.getDataSource();
        if (dataSource == null) {
            throw new IllegalStateException("JdbcTemplate 未配置 DataSource，无法探测数据库方言");
        }
        try (Connection conn = dataSource.getConnection()) {
            String product = conn.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("oracle") || product.contains("sql server")) {
                return Dialect.OFFSET_FETCH;
            }
            return Dialect.LIMIT_OFFSET;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---- 写入 ----

    @Override
    public int save(String table, Map<String, Object> data) {
        Object id = data.get("id");
        if (id != null && existsById(table, id)) {
            StringJoiner sets = new StringJoiner(",");
            List<Object> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if ("id".equals(entry.getKey())) {
                    continue;
                }
                sets.add(validateIdentifier(entry.getKey()) + "=?");
                params.add(entry.getValue());
            }
            if (sets.length() == 0) {
                return 0; // 除 id 外无其他列，无需更新
            }
            params.add(id);
            return jdbc.update("update " + validateIdentifier(table) + " set " + sets + " where id=?", params.toArray()); // NOSONAR: 标识符经 validateIdentifier 白名单，值参数绑定
        }
        StringJoiner cols = new StringJoiner(",");
        StringJoiner vals = new StringJoiner(",");
        List<Object> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            cols.add(validateIdentifier(entry.getKey()));
            vals.add("?");
            params.add(entry.getValue());
        }
        return jdbc.update("insert into " + validateIdentifier(table) + " (" + cols + ") values (" + vals + ")", params.toArray()); // NOSONAR: 标识符经 validateIdentifier 白名单，值参数绑定
    }

    @Override
    public int deleteById(String table, Object id) {
        return jdbc.update("delete from " + validateIdentifier(table) + " where id=?", id); // NOSONAR: 标识符经 validateIdentifier 白名单，值参数绑定
    }

    @Override
    public int update(String sql, Object... params) {
        return jdbc.update(sql, params);
    }

    // ===== 私有辅助方法 =====

    static String validateIdentifier(String name) {
        if (!name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid identifier: " + name);
        }
        return name;
    }

    // ---- 字段缓存（避免重复反射） ----

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static List<Field> getMappableFields(Class<?> cls) {
        return FIELD_CACHE.computeIfAbsent(cls, clazz -> {
            List<Field> fields = new ArrayList<>();
            Class<?> cur = clazz;
            while (cur != null) {
                for (Field f : cur.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        fields.add(f);
                    }
                }
                cur = cur.getSuperclass();
            }
            return fields;
        });
    }

    private <T> T mapToBean(ResultSet rs, Class<T> cls) {
        try {
            T bean = cls.getDeclaredConstructor().newInstance();
            ResultSetMetaData meta = rs.getMetaData();

            Map<String, Object> cols = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                cols.put(meta.getColumnLabel(i), rs.getObject(i));
            }

            for (Field f : getMappableFields(cls)) {
                Object val = cols.get(f.getName());
                if (val == null) {
                    val = cols.get(CharSequenceUtil.toUnderlineCase(f.getName()));
                }
                if (val != null) {
                    ReflectionUtils.makeAccessible(f);
                    setFieldValue(f, bean, val);
                }
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to " + cls.getName(), e);
        }
    }

    private void setFieldValue(Field field, Object bean, Object value) {
        Class<?> type = field.getType();
        if (value instanceof Number n) {
            if (type == Integer.class || type == int.class) ReflectionUtils.setField(field, bean, n.intValue());
            else if (type == Long.class || type == long.class) ReflectionUtils.setField(field, bean, n.longValue());
            else if (type == Float.class || type == float.class) ReflectionUtils.setField(field, bean, n.floatValue());
            else if (type == Double.class || type == double.class) ReflectionUtils.setField(field, bean, n.doubleValue());
            else if (type == Short.class || type == short.class) ReflectionUtils.setField(field, bean, n.shortValue());
            else if (type == Byte.class || type == byte.class) ReflectionUtils.setField(field, bean, n.byteValue());
            else ReflectionUtils.setField(field, bean, value);
        } else if (value instanceof Timestamp t) {
            if (type == LocalDateTime.class) ReflectionUtils.setField(field, bean, t.toLocalDateTime());
            else ReflectionUtils.setField(field, bean, t);
        } else {
            ReflectionUtils.setField(field, bean, value);
        }
    }

    private Map<String, Object> mapToMap(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int n = meta.getColumnCount();
        Map<String, Object> map = new CaseInsensitiveLinkedMap<>();
        for (int i = 1; i <= n; i++) {
            map.put(meta.getColumnLabel(i), rs.getObject(i));
        }
        return map;
    }
}

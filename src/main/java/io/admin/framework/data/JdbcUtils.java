package io.admin.framework.data;

import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils; // 引入 QueryUtils
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

/**
 * Spring Boot 环境下的原生 SQL 工具类（JdbcUtils）。
 * 基于 Spring 的 JdbcTemplate 封装，专注于执行复杂的原生 SQL 查询和更新，
 * 简化了底层 JDBC 操作和结果集映射。
 * * 优化点：
 * 1. 分页查询方法使用 QueryUtils 增强了对 Pageable 中 Sort 信息的支持。
 * 2. 方法名优化：标量查询改为 queryForXxx，动态DML明确使用 ByMap 区分。
 */
@Component
@Slf4j
public class JdbcUtils {

    @Getter
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数：通过数据源（DataSource）初始化 JdbcTemplate。
     *
     * @param dataSource 数据库连接池的数据源
     */
    public JdbcUtils(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // --- 1. DML/DDL 操作 ---

    /**
     * 执行更新、插入或删除（DML）操作。
     */
    public int update(String sql, Object... params) {
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 执行批量更新操作。
     */
    public int[] batch(String sql, List<Object[]> batchArgs) {
        if (batchArgs == null || batchArgs.isEmpty()) {
            return new int[0];
        }
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 执行不带参数的批量 SQL 语句。
     */
    public int[] batch(String sql) {
        return jdbcTemplate.batchUpdate(sql);
    }

    /**
     * 执行任意 SQL 语句，常用于 DDL 操作。
     */
    public int execute(String sql, Object... params) {
        if (params == null || params.length == 0) {
            jdbcTemplate.execute(sql);
            return 1;
        }
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 执行 SQL 语句，忽略所有 SQL 异常（静默执行）。
     */
    public int executeQuietly(String sql, Object... params) {
        try {
            return this.execute(sql, params);
        } catch (Exception e) {
            log.warn("Executable SQL failed (ignored): {} for SQL: {}", e.getMessage(), sql);
        }
        return 0;
    }

    // --- 2. 查询操作 (Query) ---

    /**
     * 查询单条记录，并将其映射到指定的 Java Bean 对象。
     */
    public <T> T findOne(Class<T> cls, String sql, Object... params) {
        try {
            RowMapper<T> rowMapper = new BeanPropertyRowMapper<>(cls);
            return jdbcTemplate.queryForObject(sql, rowMapper, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 查询单条记录，返回一个 Map 对象。
     */
    public Map<String, Object> findOne(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForMap(sql, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 查询多条记录，并将其映射到指定的 Java Bean 列表。
     */
    public <T> List<T> findAll(Class<T> cls, String sql, Object... params) {
        RowMapper<T> rsh = new BeanPropertyRowMapper<>(cls);
        List<T> list = jdbcTemplate.query(sql, rsh, params);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 查询多条记录，返回一个 Map 列表。
     */
    public List<Map<String, Object>> findAll(String sql, Object... params) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, params);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * **[优化]** 查询结果集的第一个值（第一行第一列），返回 Object。
     */
    public Object queryForObject(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, Object.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * **[优化]** 查询结果集的第一个值，并尝试转换为 Long 类型。
     */
    public Long queryForLong(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, params);
        } catch (Exception e) {
            Object result = queryForObject(sql, params);
            if (result instanceof Number) {
                return ((Number) result).longValue();
            }
            if (result != null) {
                try {
                    return Long.parseLong(result.toString());
                } catch (NumberFormatException nfe) {
                    log.warn("Scalar result could not be parsed as Long: {}", result);
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * **[优化]** 查询结果集的第一个值，并转换为 Integer 类型。
     */
    public Integer queryForInteger(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, params);
        } catch (Exception e) {
            Object result = queryForObject(sql, params);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            if (result != null) {
                try {
                    return Integer.parseInt(result.toString());
                } catch (NumberFormatException nfe) {
                    log.warn("Scalar result could not be parsed as Integer: {}", result);
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * 查询结果集的某一列，并返回该列值的列表。
     */
    public <T> List<T> findColumnList(String sql, Object... params) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = jdbcTemplate.queryForList(sql, Object.class, params);
        return (List<T>) rawList;
    }

    // --- 3. 复杂结果集和分页 ---

    /**
     * **[优化]** 查询列表，将结果集的前两列组装成一个字典 Map (Key: 第一列, Value: 第二列)。
     */
    public Map<String, Object> queryForMapDict(String sql, Object... params) {
        List<Map<String, Object>> list = this.findAll(sql, params);

        CaseInsensitiveLinkedMap<String, Object> dict = new CaseInsensitiveLinkedMap<>();

        for (Map<String, Object> row : list) {
            if (row.size() < 2) {
                throw new IllegalStateException("查询结果至少需要两列才能构建字典 Map。");
            }
            Iterator<String> ite = row.keySet().iterator();

            String k1 = ite.next();
            String k2 = ite.next();

            Object v1 = row.get(k1);
            Object v2 = row.get(k2);

            if (v1 != null) {
                dict.put(v1.toString(), v2);
            }
        }
        return dict;
    }

    /**
     * 查询列表，将结果集的 Bean 映射到一个 Map，使用 Bean 的**第一个字段**作为 Map 的键。
     */
    public <K, V> Map<K, V> findBeanMap(Class<V> cls, String sql, Object... params) {
        List<V> list = this.findAll(cls, sql, params);
        if (list.isEmpty()) return Collections.emptyMap();

        Map<K, V> map = new LinkedHashMap<>();
        try {
            Field keyField = null;
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    keyField = field;
                    break;
                }
            }

            if (keyField == null) {
                throw new IllegalStateException("Class " + cls.getName() + " does not have any non-static fields.");
            }

            keyField.setAccessible(true);
            for (V bean : list) {
                @SuppressWarnings("unchecked")
                K key = (K) keyField.get(bean);
                map.put(key, bean);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract key from bean for BeanMap: " + cls.getName(), e);
        }
        return map;
    }

    /**
     * **[优化]** 查询列表，将结果集的每一行 Map 映射到一个总 Map，使用**第一列**的值作为键。
     */
    public <K> Map<K, Map<String, Object>> queryForMapKeyed(String sql, Object... params) {
        List<Map<String, Object>> list = this.findAll(sql, params);
        if (list.isEmpty()) return Collections.emptyMap();

        Map<K, Map<String, Object>> keyedMap = new LinkedHashMap<>();
        for (Map<String, Object> row : list) {
            if (row.isEmpty()) continue;

            String firstKeyName = row.keySet().iterator().next();
            Object keyObject = row.get(firstKeyName);

            if (keyObject == null) {
                log.warn("First column value is null, skipping row.");
                continue;
            }

            @SuppressWarnings("unchecked")
            K key = (K) keyObject;
            keyedMap.put(key, row);
        }
        return keyedMap;
    }

    /**
     * **[优化]** 执行分页查询，返回 Spring Data Page 对象 (Bean)。
     */
    public <T> Page<T> findAll(Class<T> cls, Pageable pageable, String sql, Object... params) {
        // 1. 计数
        String countSql = "select count(*) from (" + sql + ") as t_count";
        long count = this.queryForLong(countSql, params); // 使用优化后的方法名

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 拼接排序 (QueryUtils)
        String sortedSql = QueryUtils.applySorting(sql, pageable.getSort()).toString();

        // 3. 拼接分页
        String paginatedSql = appendLimitOffset(sortedSql, pageable);

        // 4. 查询数据
        List<T> list = this.findAll(cls, paginatedSql, params);

        return new PageImpl<>(list, pageable, count);
    }

    /**
     * **[优化]** 执行分页查询，返回 Spring Data Page 对象 (Map)。
     */
    public Page<Map<String, Object>> findAll(Pageable pageable, String sql, Object... params) {
        // 1. 计数
        String countSql = "select count(*) from (" + sql + ") as t_count";
        long count = this.queryForLong(countSql, params); // 使用优化后的方法名

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 拼接排序 (QueryUtils)
        String sortedSql = QueryUtils.applySorting(sql, pageable.getSort()).toString();

        // 3. 拼接分页
        String paginatedSql = appendLimitOffset(sortedSql, pageable);

        // 4. 查询数据
        List<Map<String, Object>> list = this.findAll(paginatedSql, params);

        return new PageImpl<>(list, pageable, count);
    }

    /**
     * 辅助方法：拼接 LIMIT/OFFSET 子句。
     */
    private String appendLimitOffset(String sql, Pageable pageable) {
        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        return String.format("%s LIMIT %d OFFSET %d",
                sql,
                pageSize,
                offset);
    }

    // --- 4. 动态 DML 和元数据 ---

    /**
     * 根据 Bean 数据动态构建 SQL，向指定表插入一条记录。
     */
    public int insertBean(String tableName, Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        return this.insertByMap(tableName, map); // 使用优化后的方法名
    }

    /**
     * **[优化]** 根据 Map 数据动态构建 SQL，向指定表插入一条记录。
     */
    public int insertByMap(String tableName, Map<String, Object> map) {
        if (map == null || map.isEmpty()) return 0;

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");

        List<Object> params = new ArrayList<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) continue;

            sb.append(StrUtil.toUnderlineCase(key)).append(",");
            params.add(value);
        }

        if (params.isEmpty()) {
            log.warn("No fields to insert in table: {}", tableName);
            return 0;
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(") values (");

        for (int i = 0; i < params.size(); i++) {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        return this.update(sb.toString(), params.toArray());
    }

    /**
     * 根据 Bean 数据动态构建 SQL，以 'id' 字段为条件更新一条记录。
     */
    public int updateBeanById(String table, Object bean) {
        Map<String, Object> data = convertBeanToMap(bean);
        return this.updateByMapId(table, data); // 使用优化后的方法名
    }

    /**
     * **[优化]** 根据 Map 数据动态构建 SQL，以 'id' 字段为条件更新一条记录。
     */
    public int updateByMapId(String table, Map<String, Object> data) {
        if (!data.containsKey("id") || data.get("id") == null) {
            throw new IllegalArgumentException("Update data must contain a non-null 'id' field.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(table).append(" SET ");

        Set<String> keys = data.keySet();
        List<Object> params = new ArrayList<>();

        for (String key : keys) {
            // 排除 id 字段和值为 null 的字段（不更新）
            if (key.equals("id") || data.get(key) == null) continue;

            sb.append(StrUtil.toUnderlineCase(key)).append("=?,");
            params.add(data.get(key));
        }

        if (params.isEmpty()) {
            log.warn("No non-null fields to update in table: {}", table);
            return 0;
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE id=?");
        params.add(data.get("id"));

        String sql = sb.toString();
        return this.update(sql, params.toArray());
    }

    /**
     * 获取查询结果的列标签（列名或别名）。
     */
    public String[] getKeys(String sql) {
        try {
            String safeSql = sql.replace("?", "null");
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(safeSql);

            int columnCount = rowSet.getMetaData().getColumnCount();
            String[] keys = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                keys[i - 1] = rowSet.getMetaData().getColumnLabel(i);
            }
            return keys;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to get column keys for SQL: " + sql, ex);
        }
    }

    // --- 5. 模式（Schema）和元数据操作 ---

    /**
     * 尝试删除指定的数据库表（如果存在）。
     */
    public int dropTable(String tableName) {
        return this.executeQuietly("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * 根据 Java 类结构，自动生成并执行建表 SQL，表名默认为类名的下划线命名。
     */
    public int createTable(Class<?> cls) {
        return this.createTable(cls, StrUtil.toUnderlineCase(cls.getSimpleName()));
    }

    /**
     * 根据 Java 类结构，自动生成并执行建表 SQL。
     */
    public int createTable(Class<?> cls, String tableName) {
        String sql = generateCreateTableSql(cls, tableName);
        log.info("建表SQL：\n{}", sql);
        return this.execute(sql);
    }

    // --- 6. 辅助工具方法 ---

    /**
     * 获取数据库中的所有表名。
     */
    @SneakyThrows
    public Set<String> getTableNames() {
        try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String[] types = new String[]{"TABLE"};

            Set<String> tables = new HashSet<>();
            try (ResultSet resultSet = metaData.getTables(null, null, "%", types)) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            return tables;
        }
    }

    /**
     * 将 Java Bean 转换为 Map，键为字段名。
     */
    @SneakyThrows
    private Map<String, Object> convertBeanToMap(Object bean) {
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> cls = bean.getClass();

        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }

        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = field.get(bean);
            map.put(field.getName(), value);
        }
        return map;
    }

    /**
     * 根据 Java 类结构和指定的表名，生成 CREATE TABLE SQL 语句。
     */
    public String generateCreateTableSql(Class<?> clazz, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");

        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }

        List<String> fieldDefinitions = new ArrayList<>();
        String primaryKey = null;

        for (Field field : allFields) {
            String javaFieldName = field.getName();
            String columnName = StrUtil.toUnderlineCase(javaFieldName);
            String sqlType = getSqlType(field.getType());

            String definition = "`" + columnName + "` " + sqlType;

            if ("id".equalsIgnoreCase(javaFieldName)) {
                if (field.getType() == Long.class || field.getType() == long.class ||
                        field.getType() == Integer.class || field.getType() == int.class) {
                    definition += " NOT NULL AUTO_INCREMENT";
                } else {
                    definition += " NOT NULL";
                }
                primaryKey = columnName;
            } else if (field.getType().isPrimitive()) {
                definition += " NOT NULL";
            }

            fieldDefinitions.add(definition);
        }

        sb.append(String.join(",\n", fieldDefinitions));

        if (primaryKey != null) {
            sb.append(",\n").append("PRIMARY KEY (`").append(primaryKey).append("`)");
        }

        sb.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        return sb.toString();
    }

    /**
     * 将 Java 类型映射到常见的 MySQL/SQL 数据库类型。
     */
    public static String getSqlType(Class<?> cls) {
        if (cls == null) return "VARCHAR(255)";

        if (Enum.class.isAssignableFrom(cls)) return "VARCHAR(50)";
        if (BigDecimal.class.isAssignableFrom(cls)) return "DECIMAL(19,4)";
        if (String.class.isAssignableFrom(cls)) return "VARCHAR(255)";

        String typeName = cls.getSimpleName().toLowerCase();

        return switch (typeName) {
            case "byte", "short", "int", "integer" -> "INT";
            case "long", "biginteger" -> "BIGINT";
            case "float" -> "FLOAT";
            case "double", "bigdecimal" -> "DOUBLE";
            case "boolean" -> "TINYINT(1)";
            case "char" -> "CHAR(1)";
            case "date", "timestamp", "localdatetime" -> "DATETIME(6)";
            case "localdate" -> "DATE";
            case "localtime" -> "TIME";
            case "list", "set", "map", "byte[]" -> "LONGBLOB";
            default -> {
                if (cls.isPrimitive() || cls.getName().startsWith("java.lang.")) {
                    throw new IllegalStateException("Unsupported Java type: " + cls.getName());
                }
                yield "TEXT";
            }
        };
    }
}
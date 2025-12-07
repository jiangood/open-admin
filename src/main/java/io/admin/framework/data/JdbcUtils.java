package io.admin.framework.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.util.StrUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.spi.Limit;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Boot 环境下的原生 SQL 工具类（JdbcUtils）。
 * 基于 Spring 的 JdbcTemplate 封装，专注于执行复杂的原生 SQL 查询和更新。
 * 命名风格统一为 Find 风格。
 */
@Component
@Slf4j
public class JdbcUtils {

    private final JdbcTemplate jdbcTemplate;

    private final Dialect dialect;
    private final TypeConfiguration typeConfiguration;

    /**
     * 构造函数：通过数据源（DataSource）初始化 JdbcTemplate。
     *
     * @param dataSource           数据库连接池的数据源
     * @param entityManagerFactory JPA 的 EntityManagerFactory
     */
    public JdbcUtils(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);

        dialect = sessionFactory.getJdbcServices().getDialect();
        typeConfiguration = sessionFactory.getTypeConfiguration();
    }

    // --- 1. DML 和通用执行操作 ---

    /**
     * 执行更新、插入或删除（DML）操作。
     */
    public int update(String sql, Object... params) {
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 执行批量更新操作。
     */
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        if (batchArgs == null || batchArgs.isEmpty()) {
            return new int[0];
        }
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 执行不带参数的批量 SQL 语句。
     */
    public int[] batchUpdate(String sql) {
        return jdbcTemplate.batchUpdate(sql);
    }

    /**
     * 执行任意 SQL 语句，常用于 DDL 操作或不带参数的 DML/Select。
     * 优化：统一使用 update 方法处理带参数的 DML。
     */
    public int execute(String sql, Object... params) {
        if (params == null || params.length == 0) {
            // 用于 DDL (CREATE/DROP) 或不带参数的 DML/Select
            jdbcTemplate.execute(sql);
            return 1;
        }
        // 用于 DML (INSERT/UPDATE/DELETE)
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

    // --- 2. 查询操作 (Find) ---

    /**
     * 查询单条记录，并将其映射到指定的 Java Bean 对象。
     *
     * @return 映射后的 Bean 对象，如果没有找到则返回 null。
     */
    @Nullable
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
     *
     * @return 映射后的 Map 对象，如果没有找到则返回 null。
     */
    @Nullable
    public Map<String, Object> findOneMap(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForMap(sql, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 查询多条记录，并将其映射到指定的 Java Bean 列表或 Map 列表。
     * 优化：移除冗余的 Map 类型判断，让用户调用 findAllMap。
     */
    public <T> List<T> findAll(Class<T> cls, String sql, Object... params) {
        // 如果用户传入 Map.class，BeanPropertyRowMapper 也可以处理
        // 或者用户应调用 findAllMap()
        RowMapper<T> rsh = new BeanPropertyRowMapper<>(cls);
        return jdbcTemplate.query(sql, rsh, params);
    }

    /**
     * 查询多条记录，返回一个 List<Map<String, Object>> 列表。
     */
    public List<Map<String, Object>> findAllMap(String sql, Object... params) {
        return jdbcTemplate.queryForList(sql, params);
    }

    /**
     * 查询结果集的第一个值（第一行第一列），返回 Object。
     *
     * @return 标量值，如果没有结果则返回 null。
     */
    @Nullable
    public Object findScalar(String sql, Object... params) {
        try {
            // 注意：当查询结果集为空时，queryForObject 会抛出 EmptyResultDataAccessException
            return jdbcTemplate.queryForObject(sql, Object.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 查询结果集的第一个值，并尝试转换为 Long 类型。
     */
    @Nullable
    public Long findLong(String sql, Object... params) {
        Object o = this.findScalar(sql, params);
        return Convert.toLong(o);
    }

    /**
     * 查询结果集的第一个值，并转换为 Integer 类型。
     */
    @Nullable
    public Integer findInteger(String sql, Object... params) {
        Object o = this.findScalar(sql, params);
        return Convert.toInt(o);
    }

    /**
     * 查询结果集的某一列，并返回该列值的列表。
     *
     * @param elementType 列表元素的类型
     */
    public <T> List<T> findColumnList(Class<T> elementType, String sql, Object... params) {
        return jdbcTemplate.queryForList(sql, elementType, params);
    }

    /**
     * 检查记录是否存在。
     *
     * @return 如果至少有一条记录匹配，则返回 true。
     */
    public boolean exists(String sql, Object... params) {
        String existsSql = String.format("SELECT EXISTS (%s)", sql);
        // EXISTS 返回 1 或 0
        Integer result = this.findInteger(existsSql, params);
        return result != null && result > 0;
    }

    // --- 3. 复杂结果集和分页 ---

    /**
     * 查询列表，将结果集的前两列组装成一个字典 Map (Key: 第一列, Value: 第二列)。
     */
    public Map<String, Object> findMapDict(String sql, Object... params) {
        List<Map<String, Object>> list = this.findAllMap(sql, params);

        CaseInsensitiveLinkedMap<String, Object> dict = new CaseInsensitiveLinkedMap<>();

        for (Map<String, Object> row : list) {
            if (row.size() < 2) {
                throw new IllegalStateException("查询结果至少需要两列才能构建字典 Map。");
            }
            List<String> keys = new ArrayList<>(row.keySet());
            String k1 = keys.get(0);
            String k2 = keys.get(1);

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
            // 查找第一个非静态字段作为键
            Field keyField = Arrays.stream(cls.getDeclaredFields())
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Class " + cls.getName() + " does not have any non-static fields."));

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
     * 查询列表，将结果集的每一行 Map 映射到一个总 Map，使用**第一列**的值作为键。
     * 优化：使用 Stream API 简化实现。
     */
    public <K> Map<K, Map<String, Object>> findMapKeyed(String sql, Object... params) {
        List<Map<String, Object>> list = this.findAllMap(sql, params);
        if (list.isEmpty()) return Collections.emptyMap();

        return list.stream()
                .filter(row -> !row.isEmpty())
                .collect(Collectors.toMap(
                        row -> {
                            String firstKeyName = row.keySet().iterator().next();
                            Object keyObject = row.get(firstKeyName);
                            if (keyObject == null) {
                                log.warn("First column value is null, skipping row.");
                                return null;
                            }
                            // 强制类型转换，可能抛出 ClassCastException，但通常用户会确保类型正确
                            @SuppressWarnings("unchecked")
                            K key = (K) keyObject;
                            return key;
                        },
                        row -> row, // Value 是整行 Map
                        (existing, replacement) -> {
                            // Key 冲突处理：使用替换值，并给出警告
                            log.warn("Duplicate key found in findMapKeyed, key will be replaced.");
                            return replacement;
                        },
                        LinkedHashMap::new // 保持原始的插入顺序
                ));
    }

    /**
     * 执行分页查询，返回 Spring Data Page 对象 (Bean)。
     */
    public <T> Page<T> findAll(Class<T> cls, Pageable pageable, String sql, Object... params) {
        // 1. 计数
        // 注意：这里使用 AS t_count 是为了避免在某些数据库中子查询必须有别名的问题
        String countSql = "select count(*) from (" + sql + ") as t_count";
        long count = this.findLong(countSql, params);

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 拼接排序 (QueryUtils)
        String sortedSql = QueryUtils.applySorting(sql, pageable.getSort());

        // 3. 拼接分页
        String paginatedSql = appendLimitOffset(sortedSql, pageable);

        // 4. 查询数据
        List<T> list = this.findAll(cls, paginatedSql, params);

        return new PageImpl<>(list, pageable, count);
    }

    /**
     * 执行分页查询，返回 Spring Data Page 对象 (Map)。
     */
    public Page<Map> findAll(Pageable pageable, String sql, Object... params) {
        return this.findAll(Map.class, pageable, sql, params);
    }

    /**
     * 辅助方法：拼接 LIMIT/OFFSET 子句。
     */
    private String appendLimitOffset(String sql, Pageable p) {
        // 使用 Hibernate 的 Dialect 实现数据库特有的分页语法
        return dialect.getLimitHandler().processSql(sql, new Limit((int) p.getOffset(), p.getPageSize()));
    }

    // --- 4. 动态 DML (Insert/Update) ---

    /**
     * 根据 Bean 数据动态构建 SQL，向指定表插入一条记录。
     */
    public int insert(String tableName, Object bean) {
        Map<String, Object> map = convertBeanToMap(bean);
        return this.insert(tableName, map);
    }

    /**
     * 根据 Map 数据动态构建 SQL，向指定表插入一条记录。
     */
    public int insert(String tableName, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return 0;
        }

        // 过滤掉值为 null 的字段
        Map<String, Object> filteredMap = map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredMap.isEmpty()) {
            log.warn("No non-null fields to insert in table: {}", tableName);
            return 0;
        }

        List<String> columns = filteredMap.keySet().stream()
                .map(StrUtil::toUnderlineCase)
                .collect(Collectors.toList());

        List<Object> params = new ArrayList<>(filteredMap.values());

        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(params.size(), "?"));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnNames, placeholders);

        return this.update(sql, params.toArray());
    }

    /**
     * 根据 Bean 数据动态构建 SQL，以 'id' 字段为条件更新一条记录。
     */
    public int updateById(String table, Object bean) {
        Map<String, Object> data = convertBeanToMap(bean);
        return this.updateById(table, data);
    }

    /**
     * 根据 Map 数据动态构建 SQL，以 'id' 字段为条件更新一条记录。
     */
    public int updateById(String table, Map<String, Object> data) {
        if (!data.containsKey("id") || data.get("id") == null) {
            throw new IllegalArgumentException("Update data must contain a non-null 'id' field.");
        }

        // 提取 ID
        Object idValue = data.get("id");

        // 移除 ID 字段，剩下的用于 SET 语句
        Map<String, Object> updateFields = data.entrySet().stream()
                .filter(entry -> !"id".equalsIgnoreCase(entry.getKey()) && entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (updateFields.isEmpty()) {
            log.warn("No non-null fields to update in table: {}", table);
            return 0;
        }

        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
            setClause.append(StrUtil.toUnderlineCase(entry.getKey())).append("=?,");
            params.add(entry.getValue());
        }

        setClause.deleteCharAt(setClause.length() - 1); // 移除末尾逗号

        params.add(idValue); // 添加 ID 作为 WHERE 条件的参数

        String sql = String.format("UPDATE %s SET %s WHERE id=?", table, setClause);

        return this.update(sql, params.toArray());
    }

    /**
     * 根据 Bean 数据动态构建 SQL，执行通用更新（需手动指定 WHERE 条件）。
     *
     * @param table       表名
     * @param bean        包含更新字段和值的 Bean
     * @param whereClause WHERE 子句 (例如: "name=? AND age>?" )
     * @param whereParams WHERE 子句中的参数值
     * @return 更新的行数
     */
    public int update(String table, Object bean, String whereClause, Object... whereParams) {
        Map<String, Object> data = convertBeanToMap(bean);
        return this.update(table, data, whereClause, whereParams);
    }

    /**
     * 根据 Map 数据动态构建 SQL，执行通用更新（需手动指定 WHERE 条件）。
     *
     * @param table       表名
     * @param data        包含更新字段和值的 Map
     * @param whereClause WHERE 子句 (例如: "name=? AND age>?" )
     * @param whereParams WHERE 子句中的参数值
     * @return 更新的行数
     */
    public int update(String table, Map<String, Object> data, String whereClause, Object... whereParams) {

        Map<String, Object> updateFields = data.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (updateFields.isEmpty()) {
            log.warn("No non-null fields to update in table: {}", table);
            return 0;
        }

        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
            setClause.append(StrUtil.toUnderlineCase(entry.getKey())).append("=?,");
            params.add(entry.getValue());
        }

        setClause.deleteCharAt(setClause.length() - 1); // 移除末尾逗号

        // 添加 WHERE 参数
        if (whereParams != null && whereParams.length > 0) {
            params.addAll(Arrays.asList(whereParams));
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s", table, setClause, whereClause);

        return this.update(sql, params.toArray());
    }


    // --- 5. Schema（模式）和元数据操作 ---

    /**
     * 获取查询结果的列标签（列名或别名）。
     */
    public String[] getColumnLabels(String sql) {
        try {
            // 为了安全获取元数据，替换掉占位符 '?'
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

    /**
     * 检查指定的表是否存在。
     */
    public boolean tableExists(String tableName) {
        try {
            // 尝试同时检查大写和小写，以适应不同数据库的表名敏感性
            Set<String> tableNames = this.getTableNames();
            return tableNames.contains(tableName.toUpperCase())
                    || tableNames.contains(tableName.toLowerCase())
                    || tableNames.contains(tableName); // 原始大小写
        } catch (Exception e) {
            log.warn("Error checking table existence for {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 检查指定的表是否包含某列。
     */
    @SneakyThrows
    public boolean columnExists(String tableName, String columnName) {
        try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 尝试获取表元数据。
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                return rs.next();
            }
        } catch (DataAccessException e) {
            // 如果表名不正确或驱动不支持，可能抛出异常。
            log.debug("Could not determine column existence for table {}: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 尝试删除指定的数据库表（如果存在）。
     */
    public int dropTable(String tableName) {
        return this.executeQuietly("DROP TABLE IF EXISTS " + tableName);
    }

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

    // --- 6. 辅助工具方法 ---

    /**
     * 将 Java Bean 转换为 Map，键为字段名。
     * 优化：使用 Hutool 的 BeanUtil 替换手动反射。
     */
    private Map<String, Object> convertBeanToMap(Object bean) {
        // 排除 class 属性，并保留 null 值（如果需要）。默认不包含 null 值。
        // 如果需要包含 null 值，请使用: BeanUtil.beanToMap(bean, new HashMap<>(), true, false);
        return BeanUtil.beanToMap(bean);
    }

    /**
     * 根据 Java 类结构，生成 CREATE TABLE SQL 语句。
     * 提示：此方法依赖 Hibernate 内部 API，可能在版本升级时失效。
     */
    public String generateCreateTableSql(Class<?> cls, String tableName) {
        List<Field> allFields = new ArrayList<>();

        // 1. 收集所有非静态、非瞬态字段（包括父类）
        ReflectionUtils.doWithFields(cls, allFields::add,
                field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()));

        if (allFields.isEmpty()) {
            throw new IllegalArgumentException("Class " + cls.getName() + " has no fields for table creation.");
        }

        Field primaryKeyField = allFields.get(0);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");

        // 2. 遍历字段，生成列定义
        for (Field field : allFields) {
            String columnName = StrUtil.toUnderlineCase(field.getName());
            String sqlDataType = mapJavaTypeToSql(field.getType());

            sql.append("    `").append(columnName).append("` ").append(sqlDataType);

            // 3. 处理主键和 NOT NULL
            if (field == primaryKeyField) {
                sql.append(" NOT NULL");
            } else {
                sql.append(" NULL");
            }

            sql.append(",\n");
        }

        // 4. 补上主键定义
        String pkColumnName = StrUtil.toUnderlineCase(primaryKeyField.getName());
        sql.append("    PRIMARY KEY (`").append(pkColumnName).append("`)\n");

        // 结束语句
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

        return sql.toString();
    }

    /**
     * 使用 Hibernate Dialect 内部的类型映射，将 Java 类型转换为对应的 SQL 类型名称。
     */
    private String mapJavaTypeToSql(Class<?> javaTypeClass) {
        DdlTypeRegistry ddlTypeRegistry = typeConfiguration.getDdlTypeRegistry();
        BasicTypeRegistry basicTypeRegistry = typeConfiguration.getBasicTypeRegistry();

        BasicType hibernateType = basicTypeRegistry.getRegisteredType(javaTypeClass);
        if (hibernateType == null) {
            // 对于未注册的类型，默认映射为 String 类型
            hibernateType = basicTypeRegistry.getRegisteredType(String.class);
        }

        JdbcType jdbcType = hibernateType.getJdbcType();

        String typeName = ddlTypeRegistry.getTypeName(jdbcType.getDdlTypeCode(), dialect);

        return typeName;
    }
}
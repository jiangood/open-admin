package io.github.jiangood.openadmin.lang.jdbc;


import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;


@Getter
@Slf4j
public class DbTool {

    private final QueryRunner runner;

    public DbTool(DataSource dataSource) {
        this.runner = new QueryRunner(dataSource);
    }

    public static String getSqlType(Class<?> cls) {
        if (Enum.class.isAssignableFrom(cls)) {
            return "varchar(50)";
        }
        if (BigDecimal.class.isAssignableFrom(cls)) {
            return "decimal(10,2)";
        }


        String typeName = cls.getSimpleName().toLowerCase();
        switch (typeName) {
            case "byte":
            case "short":
            case "int":
            case "integer":
                return "INT";
            case "long":
                return "BIGINT";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            case "boolean":
                return "BOOLEAN";
            case "char":
            case "character":
            case "string":
                return "VARCHAR(255)";
            case "date":
                return "datetime(6)";


            // support collection, convert to string
            case "list":
            case "set":
                return "text";

            default:
                throw new IllegalStateException("not support field type " + cls);
        }
    }

    public int dropTable(String tableName) {
        return this.execute("DROP TABLE IF EXISTS " + tableName);
    }

    public int createTable(Class<?> cls) {
        return this.createTable(cls, StrUtil.toUnderlineCase(cls.getSimpleName()));
    }

    public <T> T findOne(Class<T> cls, String sql, Object... params) {
        params = checkParam(params);


        ResultSetHandler<T> rsh = new BeanHandler<>(cls);

        return this.query(sql, rsh, params);
    }

    /***
     *  returns a Map of Beans
     *
     * 查询列表，返回一个map，启用主键作为 map的key， bean 作为map的值
     *
     *  例如 select id, name, age from user;
     *
     *  返回的就是 {
     *      1: {name: "张三”, age: 19}.
     *      2: {name: "李四“, age: 24}
     *  }
     *
     *  @param <K>
     *       the type of keys maintained by the returned map
     *  @param <V>
     *       the type of the bean
     *
     * @see BeanMapHandler
     *
     */
    public <K, V> Map<K, V> findBeanMap(Class<V> cls, String sql, Object... params) {
        params = checkParam(params);

        BeanMapHandler<K, V> beanMapHandler = new BeanMapHandler<>(cls);

        return this.query(sql, beanMapHandler, params);
    }

    /***
     *  returns a Map of Maps
     *
     *  例如 select id, name, age from user;
     *
     *  返回的就是 {
     *      1: {name: "张三”, age: 19}.
     *      2: {name: "李四“, age: 24}
     *  }
     *
     *
     * @see BeanMapHandler
     *
     */
    public <K> Map<K, Map<String, Object>> findKeyed(String sql, Object... params) {
        params = checkParam(params);

        KeyedHandler<K> handler = new KeyedHandler<>();

        Map<K, Map<String, Object>> result = this.query(sql, handler, params);

        return result;
    }

    /**
     * 返回字典， 大小写不敏感
     * 先查询列表，将前两个字段组装成map
     * <p>
     * 如 select id, name from user
     * <p>
     * 结果
     * { 1: "张三”,  2: "李四" }
     *
     * @param sql
     * @param params
     * @return
     */
    public Map<String, Object> findTwoFieldListToDict(String sql, Object... params) {
        List<Map<String, Object>> list = this.findAll(sql, params);

        // 大小写不敏感
        CaseInsensitiveLinkedMap<String, Object> dict = new CaseInsensitiveLinkedMap<>();

        for (Map<String, Object> row : list) {
            if (row.size() < 2) {
                throw new IllegalStateException("result size error");
            }
            Iterator<String> ite = row.keySet().iterator();

            String k1 = ite.next();
            String k2 = ite.next();

            Object v1 = row.get(k1);
            Object v2 = row.get(k2);
            dict.put((String) v1, v2);
        }
        return dict;
    }

    public <T> List<T> findAll(Class<T> cls, String sql, Object... params) {
        params = checkParam(params);
        ResultSetHandler<List<T>> rsh = new BeanListHandler<>(cls);

        List<T> list = this.query(sql, rsh, params);

        return list == null ? Collections.emptyList() : list;
    }

    public <T> List<T> findAll(Class<T> cls, String sql, List<Object> params) {
        return this.findAll(cls, sql, params.toArray());
    }

    public List<Map<String, Object>> findAll(String sql, Object... params) {
        params = checkParam(params);

        ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler();
        List<Map<String, Object>> list = this.query(sql, rsh, params);
        if (list == null) {
            return Collections.emptyList();
        }

        return list;
    }

    // 这里默认使用mysql的分页
    public <T> Page<T> findAll(Class<T> cls, Pageable pageable, String sql, Object... params) {
        params = checkParam(params);

        String countSql = "select count(*) from (" + sql + ") as t";
        long count = this.findLong(countSql, params);

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }


        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();
        sql = sql + " limit " + pageSize + " offset " + offset;
        ResultSetHandler<List<T>> rsh = new BeanListHandler<>(cls);
        List<T> list = this.query(sql, rsh, params);

        return new PageImpl<>(list, pageable, count);
    }

    public Map<String, Object> findOne(String sql, Object... params) {
        params = checkParam(params);
        MapHandler rsh = new MapHandler();
        Map<String, Object> map = this.query(sql, rsh, params);

        return map;
    }

    /**
     * 返回 单个值
     * 如 select count(*) from user
     */
    public Object findScalar(String sql, Object... params) {
        params = checkParam(params);

        return this.query(sql, new ScalarHandler<>(), params);
    }

    public Long findLong(String sql, Object... params) {
        params = checkParam(params);

        Object result = this.query(sql, new ScalarHandler<>(), params);
        if (result instanceof Long) {
            return (Long) result;
        }

        if (result instanceof Integer) {
            return ((Integer) result).longValue();
        }

        if (result != null) {
            return Long.parseLong(result.toString());
        }

        return null;

    }

    public Integer findInteger(String sql, Object... params) {
        Long l = this.findLong(sql, params);
        if (l != null) {
            return l.intValue();
        }
        return null;
    }

    /**
     * 查询某一列
     *
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> List<T> findColumnList(String sql, Object... params) {
        params = checkParam(params);
        List<T> list = null;
        list = this.query(sql, new ColumnListHandler<>(), params);
        return list == null ? Collections.emptyList() : list;
    }

    @SneakyThrows
    public int update(String sql, Object... params) {
        params = checkParam(params);

        return getRunner().update(sql, params);
    }

    @SneakyThrows
    public int[] batch(String sql, Object[][] params) {
        if (params == null) {
            params = new Object[0][0];
        }
        return getRunner().batch(sql, params);
    }

    @SneakyThrows
    public int[] batch(String sql) {
        return getRunner().batch(sql, new Object[0][0]);
    }

    @SneakyThrows
    public int execute(String sql, Object... params) {
        return getRunner().execute(sql, params);
    }

    public int executeQuietly(String sql, Object... params) {
        try {
            return getRunner().execute(sql, params);
        } catch (SQLException e) {
            log.error("可忽略的sql异常 {}", e.getMessage());
            log.error(sql);
        }
        return 0;
    }

    public String[] getKeys(String sql) {
        sql = sql.replace("?", "''");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getRunner().getDataSource().getConnection();

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();

            String[] ks = new String[metaData.getColumnCount()];
            for (int i = 1; i <= ks.length; i++) {
                ks[i - 1] = metaData.getColumnLabel(i);
            }
            return ks;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            org.apache.commons.dbutils.DbUtils.closeQuietly(conn, ps, rs);
        }

    }

    @SuppressWarnings("rawtypes")
    private Object[] checkParam(Object[] params) {
        if (params != null && params.length == 1) {
            Object object = params[0];
            if (object instanceof Collection col) {
                if (col.isEmpty()) {
                    return null;
                }
            }
        }
        return params;
    }

    /**
     * insert map data to table
     */
    public int insert(String tableName, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");


        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            sb.append(key).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        sb.append(" values (");

        for (String ignored : keySet) {
            sb.append("?,");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        return this.update(sb.toString(), map.values().toArray());
    }

    public int updateById(String table, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(table).append(" set ");

        Set<String> keys = data.keySet();

        List<Object> params = new ArrayList<>();
        for (String key : keys) {
            if ("id".equals(key)) {
                continue;
            }
            sb.append(StrUtil.toUnderlineCase(key)).append("=?,");
            params.add(data.get(key));
        }
        sb.deleteCharAt(sb.length() - 1);

        sb.append(" where id=?");
        params.add(data.get("id"));

        String sql = sb.toString();
        return this.update(sql, params.toArray());
    }


    // ------------------------------------元数据部分------------------------------

    @SneakyThrows
    public <T> T query(final String sql, final ResultSetHandler<T> rsh, final Object... params) {
        return getRunner().query(sql, rsh, params);
    }

    @SneakyThrows
    public Set<String> getTableNames() {
        try (Connection conn = this.getRunner().getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            Set<String> tables;
            try (ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"})) {

                tables = new HashSet<>();
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }

            return tables;
        }

    }

    @SneakyThrows
    public Set<String> getTableColumns(String tableName) {
        try (Connection conn = this.getRunner().getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();


            Set<String> set;
            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {

                set = new HashSet<>();
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String columnType = rs.getString("TYPE_NAME");
                    System.out.println("Column Name: " + columnName + ", Type: " + columnType);
                    set.add(columnName);
                }
            }
            return set;
        }
    }

    @SneakyThrows
    public String getTableColumnType(String tableName, String columnName) {
        try (Connection conn = this.getRunner().getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    String n = rs.getString("COLUMN_NAME");

                    if (n.equals(columnName)) {
                        String columnType = rs.getString("TYPE_NAME");
                        return columnType;
                    }
                }
            }
        }
        return null;
    }

    public int createTable(Class<?> cls, String tableName) {
        String sql = generateCreateTableSql(cls, tableName);
        log.info("建表SQL：\n{}", sql);
        return this.execute(sql);
    }

    public String generateCreateTableSql(Class<?> clazz, String tableName) {
        StringBuilder sb = new StringBuilder();


        sb.append("CREATE TABLE  ").append(tableName).append(" (\n");

        // 获取类以及父类的所有字段
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                // 排除静态字段
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }

        // 生成字段定义
        List<String> fieldDefinitions = new ArrayList<>();
        for (Field field : allFields) {
            String fieldName = field.getName();

            fieldDefinitions.add(fieldName + " " + getSqlType(field.getType()));
        }

        // 拼接字段定义
        sb.append(String.join(",\n", fieldDefinitions));

        sb.append("\n)");

        return sb.toString();
    }

    /***
     *
     * @param sql
     * @return
     */
    private boolean hasOrderBy(String sql) {
        return sql.toLowerCase().contains("order by");
    }


}

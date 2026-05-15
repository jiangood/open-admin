
package io.github.jiangood.openadmin.util.jdbc;

import lombok.Data;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DbToolTest {

    private DbTool dbTool;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dbTool = new DbTool(dataSource);
    }



    // 测试用的实体类
    @Data
    static class TestEntity {
        private Long id;
        private String name;
        private Integer age;
        private Boolean active;
        private Date createTime;
        private BigDecimal amount;
        private List<String> tags;
        private Set<String> roles;
    }

    // 测试枚举类
    enum TestEnum {
        VALUE1,
        VALUE2
    }

    @Test
    void testGetSqlType() {
        // 测试基本类型
        assertEquals("INT", DbTool.getSqlType(int.class));
        assertEquals("INT", DbTool.getSqlType(Integer.class));
        assertEquals("BIGINT", DbTool.getSqlType(long.class));
        assertEquals("BIGINT", DbTool.getSqlType(Long.class));
        assertEquals("FLOAT", DbTool.getSqlType(float.class));
        assertEquals("FLOAT", DbTool.getSqlType(Float.class));
        assertEquals("DOUBLE", DbTool.getSqlType(double.class));
        assertEquals("DOUBLE", DbTool.getSqlType(Double.class));
        assertEquals("BOOLEAN", DbTool.getSqlType(boolean.class));
        assertEquals("BOOLEAN", DbTool.getSqlType(Boolean.class));
        assertEquals("VARCHAR(255)", DbTool.getSqlType(char.class));
        assertEquals("VARCHAR(255)", DbTool.getSqlType(Character.class));
        assertEquals("VARCHAR(255)", DbTool.getSqlType(String.class));
        assertEquals("TIMESTAMP", DbTool.getSqlType(Date.class));

        // 测试 BigDecimal
        assertEquals("decimal(10,2)", DbTool.getSqlType(BigDecimal.class));

        // 测试枚举
        assertEquals("varchar(50)", DbTool.getSqlType(TestEnum.class));

        // 测试集合
        assertEquals("text", DbTool.getSqlType(List.class));
        assertEquals("text", DbTool.getSqlType(Set.class));
    }

    @Test
    void testGetSqlTypeWithUnsupportedType() {
        // 测试不支持的类型
        assertThrows(IllegalStateException.class, () -> {
            DbTool.getSqlType(Object.class);
        });
    }

    @Test
    void testCreateTableAndInsertData() {
        // 创建表
        dbTool.createTable(TestEntity.class, "test_entity");

        // 验证表已创建
        List<String> tableNames = dbTool.getTableNames();
        assertTrue(tableNames.contains("TEST_ENTITY"), "表应该被创建");
    }

    @Test
    void testInsertAndQuery() {
        // 创建表
        String tableName = "test_user";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入数据
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "张三");
        data1.put("age", 25);
        data1.put("active", true);
        data1.put("createTime", new Date());
        data1.put("amount", new BigDecimal("99.99"));
        data1.put("tags", "tag1,tag2");
        data1.put("roles", "role1,role2");

        int insertResult = dbTool.insert(tableName, data1);
        assertEquals(1, insertResult, "应该插入 1 条记录");

        // 查询所有数据
        List<Map<String, Object>> allRecords = dbTool.findAll("SELECT * FROM " + tableName);
        assertFalse(allRecords.isEmpty(), "应该查询到数据");
        assertEquals(1, allRecords.size(), "应该有 1 条记录");

        // 验证数据
        Map<String, Object> record = allRecords.get(0);
        assertEquals("张三", record.get("name"));
        assertEquals(25, ((Number) record.get("age")).intValue());
        assertTrue((Boolean) record.get("active"));
    }

    @Test
    void testFindOne() {
        String tableName = "test_find_one";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入数据
        Map<String, Object> data = new HashMap<>();
        data.put("name", "李四");
        data.put("age", 30);
        data.put("active", false);
        dbTool.insert(tableName, data);

        // 查询单条记录
        Map<String, Object> result = dbTool.findOne("SELECT * FROM " + tableName + " WHERE age = ?", 30);
        assertNotNull(result, "应该查询到一条记录");
        assertEquals("李四", result.get("name"));
        assertEquals(30, ((Number) result.get("age")).intValue());
    }

    @Test
    void testUpdate() {
        String tableName = "test_update";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("name", "王五");
        data.put("age", 28);
        data.put("active", true);
        dbTool.insert(tableName, data);

        // 更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 1);
        updateData.put("name", "王五更新");
        updateData.put("age", 29);

        int updateResult = dbTool.updateById(tableName, updateData);
        assertEquals(1, updateResult, "应该更新 1 条记录");

        // 验证更新
        Map<String, Object> result = dbTool.findOne("SELECT * FROM " + tableName + " WHERE id = ?", 1);
        assertNotNull(result);
        assertEquals("王五更新", result.get("name"));
        assertEquals(29, ((Number) result.get("age")).intValue());
    }

    @Test
    void testDelete() {
        String tableName = "test_delete";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入数据
        Map<String, Object> data = new HashMap<>();
        data.put("id",1);
        data.put("name", "赵六");
        data.put("age", 35);
        data.put("active", true);
        dbTool.insert(tableName, data);

        // 删除数据
        int deleteResult = dbTool.execute("DELETE FROM " + tableName + " WHERE id = ?", 1);
        assertEquals(1, deleteResult, "应该删除 1 条记录");

        // 验证删除
        List<Map<String, Object>> records = dbTool.findAll("SELECT * FROM " + tableName);
        assertTrue(records.isEmpty(), "应该没有记录");
    }

    @Test
    void testFindColumnList() {
        String tableName = "test_column";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入多条数据
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "用户" + i);
            data.put("age", 20 + i);
            dbTool.insert(tableName, data);
        }

        // 查询某一列
        List<Object> names = dbTool.findColumnList("SELECT name FROM " + tableName);
        assertEquals(3, names.size(), "应该有 3 条记录");
        assertTrue(names.contains("用户1"));
        assertTrue(names.contains("用户2"));
        assertTrue(names.contains("用户3"));
    }

    @Test
    void testFindLong() {
        String tableName = "test_count";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "用户" + i);
            data.put("age", 20 + i);
            dbTool.insert(tableName, data);
        }

        // 查询数量
        Long count = dbTool.findLong("SELECT COUNT(*) FROM " + tableName);
        assertNotNull(count);
        assertEquals(5, count.longValue(), "应该有 5 条记录");
    }

    @Test
    void testBatchInsert() {
        String tableName = "test_batch";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 批量插入
        String sql = "INSERT INTO " + tableName + " (name, age, active) VALUES (?, ?, ?)";
        Object[][] params = new Object[][]{
                {"批量用户 1", 25, true},
                {"批量用户 2", 26, false},
                {"批量用户 3", 27, true}
        };

        int[] results = dbTool.batch(sql, params);
        assertEquals(3, results.length, "应该执行 3 次插入");

        // 验证数据
        List<Map<String, Object>> records = dbTool.findAll("SELECT * FROM " + tableName);
        assertEquals(3, records.size(), "应该有 3 条记录");
    }

    @Test
    void testGetTableColumns() {
        String tableName = "test_columns";
        dbTool.dropTable(tableName);
        dbTool.createTable(TestEntity.class, tableName);

        // 获取表的列
        Set<String> columns = dbTool.getTableColumns(tableName);
        assertFalse(columns.isEmpty(), "应该有列定义");

        // 验证包含预期的列
        assertTrue(columns.stream().anyMatch(c -> c.equalsIgnoreCase("name")), "应该包含 name 列");
        assertTrue(columns.stream().anyMatch(c -> c.equalsIgnoreCase("age")), "应该包含 age 列");
    }

    @Test
    void testDropTable() {
        String tableName = "test_drop";
        dbTool.createTable(TestEntity.class, tableName);

        // 验证表存在
        List<String> tableNames = dbTool.getTableNames();
        assertTrue(tableNames.contains(tableName.toUpperCase()), "表应该存在");

        // 删除表
        int dropResult = dbTool.dropTable(tableName);
        assertTrue(dropResult >= 0, "删除表应该成功");

        // 验证表不存在（H2 中表名是大写的）
        List<String> afterDropTables = dbTool.getTableNames();
        assertFalse(afterDropTables.contains(tableName.toUpperCase()), "表应该被删除");
    }
}
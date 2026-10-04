package io.github.jiangood.openadmin.framework.data.impl;

import lombok.Data;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JdbcRunnerImplTest {

    private JdbcRunnerImpl db;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        dataSource = ds;
        db = new JdbcRunnerImpl(ds);
    }

    @Data
    static class TestEntity {
        private Long id;
        private String name;
        private Integer age;
        private Boolean active;
        private Date createTime;
        private BigDecimal amount;
    }

    private void createTable(String tableName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            stmt.execute("CREATE TABLE " + tableName + " (" +
                    "id BIGINT PRIMARY KEY, name VARCHAR(255), age INT, active BOOLEAN, " +
                    "create_time TIMESTAMP, amount DECIMAL(10,2))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> row(Object id, String name, Integer age) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        data.put("age", age);
        data.put("active", true);
        data.put("create_time", new Date());
        data.put("amount", new BigDecimal("99.99"));
        return data;
    }

    @Test
    void testFindByIdEntity() {
        String table = "t_find_by_id";
        createTable(table);
        db.update("insert into " + table + " (id, name, age, active, create_time, amount) values (?,?,?,?,?,?)",
                1L, "张三", 25, true, new Date(), new BigDecimal("99.99"));

        TestEntity e = db.findById(table, 1L, TestEntity.class);
        assertNotNull(e);
        assertEquals(1L, e.getId());
        assertEquals("张三", e.getName());
        assertEquals(25, e.getAge());
        assertTrue(e.getActive());
        assertNotNull(e.getCreateTime()); // create_time → createTime 下划线映射
        assertEquals(new BigDecimal("99.99"), e.getAmount());

        assertNull(db.findById(table, 999L, TestEntity.class));
    }

    @Test
    void testFindByIdMap() {
        String table = "t_find_by_id_map";
        createTable(table);
        db.update("insert into " + table + " (id, name, age) values (?,?,?)", 1L, "李四", 30);

        Map<String, Object> map = db.findById(table, 1L);
        assertNotNull(map);
        assertEquals("李四", map.get("name"));
        assertEquals(30, ((Number) map.get("age")).intValue());

        assertNull(db.findById(table, 999L));
    }

    @Test
    void testFindOne() {
        String table = "t_find_one";
        createTable(table);
        db.update("insert into " + table + " (id, name, age) values (?,?,?)", 1L, "王五", 28);

        Map<String, Object> map = db.findOne("select * from " + table + " where age = ?", 28);
        assertNotNull(map);
        assertEquals("王五", map.get("name"));

        TestEntity e = db.findOne(TestEntity.class, "select * from " + table + " where age = ?", 28);
        assertNotNull(e);
        assertEquals("王五", e.getName());

        assertNull(db.findOne("select * from " + table + " where age = ?", 999));
        assertNull(db.findOne(TestEntity.class, "select * from " + table + " where age = ?", 999));
    }

    @Test
    void testFindAll() {
        String table = "t_find_all";
        createTable(table);
        for (int i = 1; i <= 3; i++) {
            db.update("insert into " + table + " (id, name, age) values (?,?,?)", (long) i, "用户" + i, 20 + i);
        }

        List<TestEntity> entities = db.findAll(table, TestEntity.class);
        assertEquals(3, entities.size());
        assertEquals("用户1", entities.get(0).getName());

        List<TestEntity> bySql = db.findAll(TestEntity.class, "select * from " + table + " where age > ?", 21);
        assertEquals(2, bySql.size());

        List<Map<String, Object>> maps = db.findAll("select * from " + table);
        assertEquals(3, maps.size());
        assertEquals("用户2", maps.get(1).get("name"));
    }

    @Test
    void testInvalidIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> db.findById("t;drop", 1L));
        assertThrows(IllegalArgumentException.class, () -> db.findAll("t x", TestEntity.class));
    }

    @Test
    void testFindAllPage() {
        String table = "t_page";
        createTable(table);
        for (int i = 1; i <= 5; i++) {
            db.update("insert into " + table + " (id, name, age) values (?,?,?)", (long) i, "用户" + i, 20 + i);
        }

        Page<TestEntity> page = db.findAll(table, PageRequest.of(1, 2), TestEntity.class);
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertEquals(2, page.getContent().size());
        assertEquals("用户3", page.getContent().get(0).getName());

        Page<Map<String, Object>> mapPage = db.findAll(PageRequest.of(0, 2), "select * from " + table + " order by id");
        assertEquals(5, mapPage.getTotalElements());
        assertEquals(2, mapPage.getContent().size());
        assertEquals("用户1", mapPage.getContent().get(0).get("name"));
    }

    @Test
    void testFindAllPageWithSort() {
        String table = "t_page_sort";
        createTable(table);
        for (int i = 1; i <= 3; i++) {
            db.update("insert into " + table + " (id, name, age) values (?,?,?)", (long) i, "用户" + i, 20 + i);
        }

        // 表名分页：Pageable 的 Sort 应生效（age 降序 → 用户3 在前）
        Page<TestEntity> page = db.findAll(table, PageRequest.of(0, 2, Sort.by("age").descending()), TestEntity.class);
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals("用户3", page.getContent().get(0).getName());
        assertEquals("用户2", page.getContent().get(1).getName());

        // SQL 分页：Sort 同样生效
        Page<Map<String, Object>> mapPage = db.findAll(PageRequest.of(0, 2, Sort.by("age").descending()),
                "select * from " + table);
        assertEquals("用户3", mapPage.getContent().get(0).get("name"));
    }

    @Test
    void testFindAllPageEmpty() {
        String table = "t_page_empty";
        createTable(table);
        Page<TestEntity> page = db.findAll(table, PageRequest.of(0, 10), TestEntity.class);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void testCountAndExists() {
        String table = "t_count";
        createTable(table);
        assertEquals(0, db.count(table));
        assertFalse(db.existsById(table, 1L));

        db.update("insert into " + table + " (id, name) values (?,?)", 1L, "张三");
        assertEquals(1, db.count(table));
        assertTrue(db.existsById(table, 1L));
        assertFalse(db.existsById(table, 2L));
    }

    @Test
    void testFindLong() {
        String table = "t_find_long";
        createTable(table);
        for (int i = 1; i <= 5; i++) {
            db.update("insert into " + table + " (id, name) values (?,?)", (long) i, "用户" + i);
        }
        Long count = db.findLong("select count(*) from " + table);
        assertNotNull(count);
        assertEquals(5, count.longValue());

        assertNull(db.findLong("select id from " + table + " where id = ?", 999L));
    }

    @Test
    void testSaveInsert() {
        String table = "t_save_insert";
        createTable(table);

        assertEquals(1, db.save(table, row(1L, "张三", 25)));

        Map<String, Object> found = db.findById(table, 1L);
        assertNotNull(found);
        assertEquals("张三", found.get("name"));
        assertEquals(25, ((Number) found.get("age")).intValue());
    }

    @Test
    void testSaveUpdate() {
        String table = "t_save_update";
        createTable(table);
        db.save(table, row(1L, "张三", 25));

        Map<String, Object> data = new HashMap<>();
        data.put("id", 1L);
        data.put("name", "张三更新");
        data.put("age", 26);
        assertEquals(1, db.save(table, data));

        Map<String, Object> found = db.findById(table, 1L);
        assertEquals("张三更新", found.get("name"));
        assertEquals(26, ((Number) found.get("age")).intValue());
        assertEquals(1, db.count(table)); // 确认是 update 而非 insert
    }

    @Test
    void testSaveOnlyIdReturnsZero() {
        String table = "t_save_only_id";
        createTable(table);
        db.save(table, row(1L, "张三", 25));

        // data 仅含 id：无可更新列，应返回 0 而不是生成非法 SQL
        Map<String, Object> onlyId = new HashMap<>();
        onlyId.put("id", 1L);
        assertEquals(0, db.save(table, onlyId));

        // 原记录不受影响
        assertEquals("张三", db.findById(table, 1L).get("name"));
    }

    @Test
    void testDeleteById() {
        String table = "t_delete";
        createTable(table);
        db.save(table, row(1L, "赵六", 35));
        assertTrue(db.existsById(table, 1L));

        assertEquals(1, db.deleteById(table, 1L));
        assertFalse(db.existsById(table, 1L));
        assertEquals(0, db.deleteById(table, 1L));
    }

    @Test
    void testUpdate() {
        String table = "t_update_sql";
        createTable(table);
        db.save(table, row(1L, "王五", 28));

        assertEquals(1, db.update("update " + table + " set name=? where id=?", "王五改", 1L));
        assertEquals("王五改", db.findById(table, 1L).get("name"));
    }
}

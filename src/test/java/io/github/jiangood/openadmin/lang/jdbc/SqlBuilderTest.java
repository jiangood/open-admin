package io.github.jiangood.openadmin.lang.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlBuilderTest {

    @Test
    void testConstructorWithEmpty() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        assertEquals("", sqlBuilder.getSql());
        assertTrue(sqlBuilder.getParams().isEmpty());
    }

    @Test
    void testConstructorWithSql() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT * FROM user");
        assertEquals("SELECT * FROM user", sqlBuilder.getSql());
        assertTrue(sqlBuilder.getParams().isEmpty());
    }

    @Test
    void testAppend() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("SELECT * FROM user");
        sqlBuilder.append(" WHERE id = ?");
        assertEquals("SELECT * FROM user WHERE id = ?", sqlBuilder.getSql());
        assertTrue(sqlBuilder.getParams().isEmpty());
    }

    @Test
    void testAppendWithParams() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("SELECT * FROM user WHERE id = ? AND name = ?", 1, "张三");
        assertEquals("SELECT * FROM user WHERE id = ? AND name = ?", sqlBuilder.getSql());
        assertEquals(2, sqlBuilder.getParams().size());
        assertEquals(1, sqlBuilder.getParams().get(0));
        assertEquals("张三", sqlBuilder.getParams().get(1));
    }

    @Test
    void testAddParams() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("SELECT * FROM user WHERE id = ? AND name = ?");
        sqlBuilder.addParams(1, "张三");
        assertEquals("SELECT * FROM user WHERE id = ? AND name = ?", sqlBuilder.getSql());
        assertEquals(2, sqlBuilder.getParams().size());
        assertEquals(1, sqlBuilder.getParams().get(0));
        assertEquals("张三", sqlBuilder.getParams().get(1));
    }

    @Test
    void testToString() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT * FROM user");
        assertEquals("SELECT * FROM user", sqlBuilder.toString());
    }

}

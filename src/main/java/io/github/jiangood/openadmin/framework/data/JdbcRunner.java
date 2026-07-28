package io.github.jiangood.openadmin.framework.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 通用 JDBC 数据访问入口，方法签名对齐 Spring Data JPA 仓库风格。
 * <p>
 * 表名方法约定主键列名为 id；Map 返回值 key 大小写不敏感；
 * 单条查询无结果返回 null。
 */
public interface JdbcRunner {

    // ---- 查询 - 单条 ----

    /** 按 id 查询单条，映射为实体类，无结果返回 null */
    <T> T findById(String table, Object id, Class<T> cls);

    /** 按 id 查询单条，返回 Map，无结果返回 null */
    Map<String, Object> findById(String table, Object id);

    /** 查询单条，映射为实体类，无结果返回 null；多行结果时取第一行 */
    <T> T findOne(Class<T> cls, String sql, Object... params);

    /** 查询单条，返回 Map，无结果返回 null；多行结果时取第一行 */
    Map<String, Object> findOne(String sql, Object... params);

    // ---- 查询 - 列表 ----

    /** 查询整表，映射为实体类列表 */
    <T> List<T> findAll(String table, Class<T> cls);

    /** 查询列表，映射为实体类列表 */
    <T> List<T> findAll(Class<T> cls, String sql, Object... params);

    /** 查询列表，每条记录返回 Map */
    List<Map<String, Object>> findAll(String sql, Object... params);

    // ---- 查询 - 分页 ----
    // 方言按数据库产品名自动探测：MySQL/PostgreSQL/H2 等用 limit offset；
    // Oracle 12c+ / SQL Server 2012+ 用 offset fetch（SQL 无 order by 且未指定 Sort 时按首列排序兜底）。
    // Pageable 的 Sort 属性名按列名原样使用（不做命名转换），指定 Sort 时原 SQL 不应再含 order by。

    /** 整表分页查询，映射为实体类 */
    <T> Page<T> findAll(String table, Pageable pageable, Class<T> cls);

    /** 分页查询，映射为实体类；count SQL 自动包裹原 SQL */
    <T> Page<T> findAll(Class<T> cls, Pageable pageable, String sql, Object... params);

    /** 分页查询，返回 Map 列表；count SQL 自动包裹原 SQL */
    Page<Map<String, Object>> findAll(Pageable pageable, String sql, Object... params);

    // ---- 标量与存在性 ----

    /** 统计整表记录数 */
    long count(String table);

    /** 查询单行单列结果并转为 Long，无结果返回 null */
    Long findLong(String sql, Object... params);

    /** 按 id 判断记录是否存在 */
    boolean existsById(String table, Object id);

    // ---- 写入 ----

    /**
     * 保存记录（JPA save 语义）：data 含 id 且记录存在 → update，否则 insert。
     * Map key 即列名，不做命名转换。
     * data 除 id 外无其他列时不执行任何操作，返回 0。
     */
    int save(String table, Map<String, Object> data);

    /** 按 id 删除记录 */
    int deleteById(String table, Object id);

    /** 执行 update / insert / delete 语句 */
    int update(String sql, Object... params);
}

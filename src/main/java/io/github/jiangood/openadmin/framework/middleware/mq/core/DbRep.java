package io.github.jiangood.openadmin.framework.middleware.mq.core;

import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DbRep implements Rep {

    private static final String TABLE_NAME = "sys_mq_message";
    
    private final DbTool dbTool;
    
    public DbRep(DbTool dbTool) {
        this.dbTool = dbTool;
        initTable();
    }
    
    private void initTable() {
        // 创建消息表
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id BIGINT PRIMARY KEY, " +
                "topic VARCHAR(255), " +
                "tag VARCHAR(255), " +
                "message TEXT" +
                ")";
        dbTool.executeQuietly(createTableSql);
        log.info("消息表初始化完成");
    }

    @Override
    public void save(Message msg) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, topic, tag, message) VALUES (?, ?, ?, ?)";
        dbTool.update(sql, msg.getId(), msg.getTopic(), msg.getTag(), msg.getMessage());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        dbTool.update(sql, id);
    }

    @Override
    public List<Message> loadAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " order by id";
        return dbTool.findAll(Message.class, sql);
    }
}

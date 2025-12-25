package io.github.jiangood.sa.common.tools;

import cn.hutool.extra.spring.SpringUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import io.github.jiangood.sa.common.tools.db.DbTool;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;


public class IdTool implements Serializable {


    public static synchronized String nextIdByDb(String tableName, String prefix, int numLen) {
        int seq = getIdByDb(tableName, prefix) + 1;
        String id = StringUtils.leftPad(String.valueOf(seq), numLen, '0');
        return prefix + id;
    }

    private static synchronized int getIdByDb(String tableName, String prefix) {
        int codeIndex = prefix.length() + 2; // mysql substr，
        String sql = "select max(CAST(SUBSTR(id,?) as signed)) as seq  from " + tableName + " where id like ?";
        Map<String, Object> map = SpringUtil.getBean(DbTool.class).findOne(sql, codeIndex, prefix + "%");
        Object seq = map.get("seq");
        if (seq == null) {
            return 0;
        }
        return Integer.parseInt(seq.toString());

    }

    /**
     * 按时间排序的uuid
     * 对于一些数据库主键友好，如mysql
     *
     * @return
     */
    public static String uuidV7() {
        UUID uuid = UuidCreator.getTimeOrderedEpochPlus1();
        return uuid.toString().replace("-", "");
    }


}

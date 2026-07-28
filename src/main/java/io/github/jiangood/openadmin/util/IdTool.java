package io.github.jiangood.openadmin.util;

import cn.hutool.extra.spring.SpringUtil;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;


public class IdTool implements Serializable {

    private IdTool() {
    }

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static long lastTimestamp = 0;
    private static long lastRandB = 0;


    public static synchronized String nextIdByDb(String tableName, String prefix, int numLen) {
        int seq = getIdByDb(tableName, prefix) + 1;
        String id = StringUtils.leftPad(String.valueOf(seq), numLen, '0');
        return prefix + id;
    }

    private static synchronized int getIdByDb(String tableName, String prefix) {
        int codeIndex = prefix.length() + 2; // mysql substr，
        String sql = "select max(CAST(SUBSTR(id,?) as signed)) as seq  from " + tableName + " where id like ?";
        Map<String, Object> map = SpringUtil.getBean(JdbcRunner.class).findOne(sql, codeIndex, prefix + "%");
        Object seq = map.get("seq");
        if (seq == null) {
            return 0;
        }
        return Integer.parseInt(seq.toString());

    }

    /**
     * 按时间排序的 UUIDv7（RFC 9562），对 MySQL 聚簇索引友好。
     * <p>
     * 布局：48 位 Unix 毫秒时间戳 | 4 位版本(0111) | 12 位随机 | 2 位变体(10) | 62 位随机/递增
     * <p>
     * 同毫秒内 rand_b 递增 +1 保证写入顺序，无需数据库交互或外部依赖。
     *
     * @return 32 位十六进制字符串（无连字符）
     */
    public static String uuidV7() {
        LOCK.lock();
        try {
            long timestamp = System.currentTimeMillis();

            if (timestamp == lastTimestamp) {
                lastRandB++;
            } else {
                lastTimestamp = timestamp;
                lastRandB = ThreadLocalRandom.current().nextLong();
            }

            // MSB: 48 位时间戳 + 12 位随机 (rand_a)
            long msb = (timestamp << 16)
                    | (ThreadLocalRandom.current().nextLong() & 0x0fffL);
            msb = (msb & 0xffffffffffff0fffL) | 0x0000000000007000L; // version 7

            // LSB: 2 位变体(10) + 62 位随机/递增 (rand_b)
            long lsb = (lastRandB & 0x3fffffffffffffffL) | 0x8000000000000000L;

            return new UUID(msb, lsb).toString().replace("-", "");
        } finally {
            LOCK.unlock();
        }
    }


}

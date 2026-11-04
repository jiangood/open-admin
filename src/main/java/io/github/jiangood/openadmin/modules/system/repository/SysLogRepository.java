package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Repository
public interface SysLogRepository extends BaseRepository<SysLog, String> {

    /**
     * 批量查询每个用户在每个 IP 下最近的一次操作记录。
     * <p>
     * 在线用户列表按「用户 + IP」匹配最近操作：同一账号可能从多个 IP 登录，
     * 只取与当前会话 IP 一致的日志。只投影需要的字段，避免把 params/result 等大字段一并读出。
     */
    @Query("""
            SELECT l.userId AS userId, l.ip AS ip, l.operation AS operation, l.operationTime AS operationTime
            FROM SysLog l
            WHERE l.userId IN :userIds
              AND l.operationTime = (
                  SELECT MAX(l2.operationTime) FROM SysLog l2
                  WHERE l2.userId = l.userId AND l2.ip = l.ip
              )
            """)
    List<LatestOperation> findLatestByUserIds(@Param("userIds") Collection<String> userIds);

    interface LatestOperation {
        String getUserId();

        String getIp();

        String getOperation();

        LocalDateTime getOperationTime();
    }

}

package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class SysLogRepositoryTest {

    @Autowired
    private SysLogRepository sysLogRepository;

    @Test
    void findLatestByUserIds_shouldReturnLatestOperationPerUserAndIp() {
        LocalDateTime now = LocalDateTime.now();
        saveLog("user-1", "10.0.0.1", "用户-创建", now.minusMinutes(10));
        saveLog("user-1", "10.0.0.1", "用户-更新", now.minusMinutes(1));
        saveLog("user-1", "10.0.0.2", "用户-登录", now.minusMinutes(5));
        saveLog("user-2", "10.0.0.1", "角色-创建", now.minusMinutes(5));
        saveLog("user-3", "10.0.0.1", "菜单-创建", now.minusMinutes(3));

        List<SysLogRepository.LatestOperation> result = sysLogRepository.findLatestByUserIds(List.of("user-1", "user-2"));
        Map<String, Map<String, SysLogRepository.LatestOperation>> byUserAndIp = result.stream()
                .collect(Collectors.groupingBy(
                        SysLogRepository.LatestOperation::getUserId,
                        Collectors.toMap(SysLogRepository.LatestOperation::getIp, Function.identity())));

        assertEquals(3, result.size());
        assertEquals("用户-更新", byUserAndIp.get("user-1").get("10.0.0.1").getOperation());
        assertEquals("用户-登录", byUserAndIp.get("user-1").get("10.0.0.2").getOperation());
        assertEquals("角色-创建", byUserAndIp.get("user-2").get("10.0.0.1").getOperation());
        assertNotNull(byUserAndIp.get("user-1").get("10.0.0.1").getOperationTime());
    }

    private void saveLog(String userId, String ip, String operation, LocalDateTime operationTime) {
        SysLog log = new SysLog();
        log.setUserId(userId);
        log.setIp(ip);
        log.setOperation(operation);
        log.setOperationTime(operationTime);
        sysLogRepository.save(log);
    }
}

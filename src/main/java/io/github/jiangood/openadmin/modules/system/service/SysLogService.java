package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogService extends BaseService<SysLog> {

    private final SysLogRepository sysLogRepository;

    @Async("operationLogExecutor")
    public void saveOperationLogAsync(SysLog sysLog) {
        repository.save(sysLog);
    }

    /**
     * 批量查询每个用户在每个 IP 下最近的一次操作记录（userId -> ip -> 最新日志）。
     */
    public Map<String, Map<String, LatestOperation>> findLatestByUserIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<String, Map<String, LatestOperation>> result = new HashMap<>();
        for (SysLogRepository.LatestOperation item : sysLogRepository.findLatestByUserIds(userIds)) {
            if (item.getUserId() == null) {
                continue;
            }
            result.computeIfAbsent(item.getUserId(), key -> new HashMap<>())
                    .putIfAbsent(item.getIp(), new LatestOperation(
                            item.getUserId(), item.getIp(), item.getOperation(), item.getOperationTime()));
        }
        return result;
    }

    /**
     * 最近操作的精简视图，避免把操作日志的大字段带到在线用户列表。
     */
    public record LatestOperation(String userId, String ip, String operation, LocalDateTime operationTime) {
    }

    /**
     * 分页查询某个用户的操作记录（个人中心"最近操作"）。
     */
    public Page<SysLog> pageByUser(String userId, Pageable pageable) {
        Spec<SysLog> q = Spec.of();
        q.eq(SysLog.Fields.userId, userId);
        return findAll(q, pageable);
    }

    @Transactional
    public SysLog save(SysLog input, List<String> requestKeys) {
        if (input.isNew()) {
            return repository.save(input);
        }

        this.updateField(input, requestKeys);
        return repository.findById(input.getId()).orElse(null);
    }
}

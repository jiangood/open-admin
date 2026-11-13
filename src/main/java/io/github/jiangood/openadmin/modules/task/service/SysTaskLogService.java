package io.github.jiangood.openadmin.modules.task.service;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.auth.LoginTool;
import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.task.entity.SysTaskLog;
import io.github.jiangood.openadmin.modules.task.repository.SysTaskLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台任务记录：任务开始时落库（status=RUNNING），结束时补全结果。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysTaskLogService extends BaseService<SysTaskLog> {

    /**
     * 失败原因过长时截断，避免超出列长度导致记录写不进去。
     */
    private static final int MAX_ERROR_LENGTH = 1000;

    private final SysTaskLogRepository taskLogRepository;

    /**
     * 当前触发人（登录用户名）；定时任务等非请求线程返回 SYSTEM。
     */
    public String currentTriggerUser() {
        try {
            LoginUser user = LoginTool.getUser();
            return user == null ? "SYSTEM" : user.getUsername();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    public SysTaskLog findByLogId(String logId) {
        return taskLogRepository.findByLogId(logId).orElse(null);
    }

    /**
     * 开始一个任务：先落库 RUNNING 记录。
     */
    @Transactional
    public SysTaskLog start(String logId, String type, String title, String permission, String triggerUser) {
        SysTaskLog task = new SysTaskLog();
        task.setLogId(logId);
        task.setType(type);
        task.setTitle(StrUtil.sub(title, 0, 200));
        task.setStatus(SysTaskLog.STATUS_RUNNING);
        task.setPermission(permission);
        task.setTriggerUser(StrUtil.sub(triggerUser, 0, 64));
        return taskLogRepository.saveAndFlush(task);
    }

    /**
     * 结束一个任务：写入结果与耗时。任务不存在或已结束时不处理。
     * <p>
     * 必须先写库再从运行注册表移除：日志 WS/轮询检测到结束后前端刷新列表要能直接拿到终态。
     */
    @Transactional
    public void finish(String logId, boolean success, String errorMessage) {
        SysTaskLog task = taskLogRepository.findByLogId(logId).orElse(null);
        if (task == null || !task.isRunning()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(success ? SysTaskLog.STATUS_SUCCESS : SysTaskLog.STATUS_FAILED);
        task.setEndTime(now);
        if (task.getCreateTime() != null) {
            task.setTimeSpend(Duration.between(task.getCreateTime(), now).toMillis());
        }
        if (!success) {
            task.setError(StrUtil.sub(StrUtil.blankToDefault(errorMessage, "任务失败"), 0, MAX_ERROR_LENGTH));
        }
        taskLogRepository.saveAndFlush(task);
    }

    /**
     * 更新进度快照 JSON。失败只记 debug：进度展示是锦上添花，不能影响任务本身。
     */
    @Transactional
    public void progress(String logId, String progressJson) {
        try {
            taskLogRepository.findByLogId(logId).ifPresent(task -> {
                if (task.isRunning()) {
                    task.setProgress(progressJson);
                    taskLogRepository.save(task);
                }
            });
        } catch (Exception e) {
            log.debug("写入任务进度失败: logId={} {}", logId, e.getMessage());
        }
    }

    /**
     * 平台重启清扫：进程内正在跑的任务都已消失，把遗留 RUNNING 行标记为失败。
     * 日志文件还在，用户仍可通过任务中心回看中断前的输出。
     */
    @Transactional
    public int markInterrupted() {
        List<SysTaskLog> running = findAll(
                Spec.<SysTaskLog>of().eq(SysTaskLog.Fields.status, SysTaskLog.STATUS_RUNNING),
                Sort.unsorted());
        LocalDateTime now = LocalDateTime.now();
        for (SysTaskLog task : running) {
            task.setStatus(SysTaskLog.STATUS_FAILED);
            task.setEndTime(now);
            if (task.getCreateTime() != null) {
                task.setTimeSpend(Duration.between(task.getCreateTime(), now).toMillis());
            }
            task.setError("平台重启，任务中断");
            taskLogRepository.save(task);
        }
        if (!running.isEmpty()) {
            log.info("清扫平台重启遗留的运行中任务记录 {} 条", running.size());
        }
        return running.size();
    }
}

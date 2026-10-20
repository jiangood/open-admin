package io.github.jiangood.openadmin.modules.job.progress;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务运行进度注册表（内存态）
 * <p>
 * 只在内存中保存「当前/最近一次」运行状态，供前端轮询展示；执行结果本身仍由 sys_job_log 落库。
 * 重启后运行中进度丢失，页面显示为「未运行」，但执行记录完整。
 */
@Slf4j
@Component
public class JobProgressRegistry {

    /**
     * 结束态在内存中保留时长，超过后由读操作惰性清理
     */
    private static final Duration FINISHED_TTL = Duration.ofMinutes(30);

    private final Map<String, JobProgress> byJobId = new ConcurrentHashMap<>();

    /**
     * 任务开始执行，覆盖同任务的旧快照
     */
    public void start(SysJob job, String logId) {
        if (job == null || job.getId() == null) {
            return;
        }
        JobProgress p = new JobProgress();
        p.setJobId(job.getId());
        p.setJobName(job.getName());
        p.setLogId(logId);
        p.setState(JobRunState.RUNNING);
        p.setBeginTime(LocalDateTime.now(ZoneId.systemDefault()));
        p.setUpdateTime(p.getBeginTime());
        byJobId.put(job.getId(), p);
    }

    /**
     * 上报百分比进度
     */
    public void update(String jobId, Integer percent, String message) {
        update(jobId, percent, null, null, message);
    }

    /**
     * 上报「已处理/总数」进度，百分比自动计算
     */
    public void update(String jobId, long current, long total, String message) {
        Integer percent = total > 0 ? (int) Math.min(100, current * 100 / total) : null;
        update(jobId, percent, current, total, message);
    }

    private void update(String jobId, Integer percent, Long current, Long total, String message) {
        if (jobId == null) {
            return;
        }
        byJobId.computeIfPresent(jobId, (key, old) -> {
            if (!old.isRunning()) {
                // 已结束的运行不再接受进度更新（例如任务异常退出后又误调）
                return old;
            }
            JobProgress p = copy(old);
            if (percent != null) {
                p.setPercent(Math.max(0, Math.min(100, percent)));
            }
            if (current != null) {
                p.setCurrent(current);
            }
            if (total != null) {
                p.setTotal(total);
            }
            if (message != null) {
                p.setMessage(message);
            }
            p.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
            p.setRunTime(elapsed(p.getBeginTime()));
            return p;
        });
    }

    /**
     * 任务执行成功
     */
    public void success(String jobId, String result) {
        finish(jobId, JobRunState.SUCCESS, result, 100);
    }

    /**
     * 任务执行失败
     */
    public void fail(String jobId, String message) {
        finish(jobId, JobRunState.FAILED, message, null);
    }

    /**
     * 仅当仍处于运行中时置为失败，用于兜底（如任务抛出 Error）
     */
    public void failIfRunning(String jobId, String message) {
        if (jobId == null) {
            return;
        }
        byJobId.computeIfPresent(jobId, (key, old) -> {
            if (!old.isRunning()) {
                return old;
            }
            return finished(old, JobRunState.FAILED, message, null);
        });
    }

    private void finish(String jobId, JobRunState state, String result, Integer percent) {
        if (jobId == null) {
            return;
        }
        byJobId.computeIfPresent(jobId, (key, old) -> finished(old, state, result, percent));
    }

    private JobProgress finished(JobProgress old, JobRunState state, String result, Integer percent) {
        JobProgress p = copy(old);
        p.setState(state);
        p.setResult(result);
        if (percent != null) {
            p.setPercent(percent);
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        p.setEndTime(now);
        p.setUpdateTime(now);
        p.setRunTime(elapsed(p.getBeginTime()));
        return p;
    }

    /**
     * 查询单个任务进度，不存在返回 null
     */
    public JobProgress get(String jobId) {
        if (jobId == null) {
            return null;
        }
        sweep();
        return byJobId.get(jobId);
    }

    /**
     * 按日志 id 查询运行进度，用于判断某份日志对应的任务是否仍在运行；不存在返回 null。
     * <p>
     * 内存态数据量很小（只保留当前/最近一次），直接遍历即可，无需额外索引。
     */
    public JobProgress getByLogId(String logId) {
        if (logId == null) {
            return null;
        }
        sweep();
        for (JobProgress p : byJobId.values()) {
            if (logId.equals(p.getLogId())) {
                return p;
            }
        }
        return null;
    }

    /**
     * 移除任务进度（任务被删除时调用）
     */
    public void remove(String jobId) {
        if (jobId != null) {
            byJobId.remove(jobId);
        }
    }

    /**
     * 查询全部进度快照，按开始时间倒序
     */
    public List<JobProgress> listAll() {
        sweep();
        List<JobProgress> list = new ArrayList<>(byJobId.values());
        list.sort(Comparator.comparing(JobProgress::getBeginTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    /**
     * 惰性清理过期的结束态快照
     */
    private void sweep() {
        LocalDateTime deadline = LocalDateTime.now(ZoneId.systemDefault()).minus(FINISHED_TTL);
        byJobId.entrySet().removeIf(e -> {
            JobProgress p = e.getValue();
            return !p.isRunning() && p.getEndTime() != null && p.getEndTime().isBefore(deadline);
        });
    }

    private static JobProgress copy(JobProgress old) {
        JobProgress p = new JobProgress();
        p.setJobId(old.getJobId());
        p.setJobName(old.getJobName());
        p.setLogId(old.getLogId());
        p.setState(old.getState());
        p.setPercent(old.getPercent());
        p.setCurrent(old.getCurrent());
        p.setTotal(old.getTotal());
        p.setMessage(old.getMessage());
        p.setBeginTime(old.getBeginTime());
        p.setUpdateTime(old.getUpdateTime());
        p.setEndTime(old.getEndTime());
        p.setRunTime(old.getRunTime());
        p.setResult(old.getResult());
        return p;
    }

    private static Long elapsed(LocalDateTime begin) {
        if (begin == null) {
            return null;
        }
        return Duration.between(begin, LocalDateTime.now(ZoneId.systemDefault())).toMillis();
    }
}

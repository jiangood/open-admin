package io.github.jiangood.openadmin.modules.system.job;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobResult;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@DisallowConcurrentExecution
@JobDescription(label = "清理临时上传文件", params = {})
public class CleanTempFileJob extends BaseJob {

    /** 每页处理的文件数量，避免文件过多时一次性加载到内存 */
    private static final int PAGE_SIZE = 1000;

    @Resource
    private SysFileRepository sysFileRepository;
    @Resource
    private SysFileService sysFileService;
    @Resource
    private JdbcRunner jdbcRunner;
    @Resource
    private SystemProperties systemProperties;

    @Override
    public JobResult execute(JobDataMap data, Logger logger) throws Exception {
        int cleanMinutes = systemProperties.getFile().getCleanTempMinutes();
        LocalDateTime deadline = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(cleanMinutes);

        Stats stats = new Stats();

        // 1. 标记超时临时文件为待删除（TEMP -> PENDING_DELETE）
        stats.tempCount = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, deadline);

        // 2. 孤儿扫描：分页只读扫描，孤儿逐个标记待删除
        markOrphans(logger, stats);

        // 3. 删除待删文件：逐文件物理删除并删库行
        deletePendingFiles(stats);

        String summary = "清理完成，标记临时 " + stats.tempCount + " 个，标记孤儿 " + stats.orphanCount
                + " 个，删除待删 " + stats.deletedCount + " 个，失败 " + stats.failureCount + " 个";
        logger.info(summary);
        return stats.failureCount > 0 ? JobResult.partial(summary) : JobResult.ok(summary);
    }

    /**
     * 扫描在用文件，业务记录已不存在的逐个标记待删除（IN_USE -> PENDING_DELETE）
     */
    private void markOrphans(Logger logger, Stats stats) {
        for (SysFile file : sysFileRepository.findByStatus(FileStatus.IN_USE)) {
            try {
                if (!jdbcRunner.existsById(file.getJoinTable(), file.getJoinId())) {
                    sysFileRepository.updateStatusByObjectNames(List.of(file.getObjectName()), FileStatus.PENDING_DELETE);
                    stats.orphanCount++;
                }
            } catch (Exception e) {
                stats.failureCount++;
                logger.error("检查业务记录存在性失败: joinTable={}, joinId={}, error={}",
                        file.getJoinTable(), file.getJoinId(), e.getMessage());
            }
        }
    }

    /**
     * 删除待删文件：反复取第一页（offset 固定 0，删掉的行让剩余记录前移），
     * 逐文件物理删除并删库行，失败的文件保留 PENDING_DELETE 待下轮重试
     */
    private void deletePendingFiles(Stats stats) {
        while (true) {
            List<SysFile> content = sysFileRepository.findByStatus(FileStatus.PENDING_DELETE, PageRequest.of(0, PAGE_SIZE)).getContent();
            if (content.isEmpty()) {
                break;
            }
            int deletedRound = 0;
            for (SysFile file : content) {
                if (sysFileService.deleteFileInternal(file)) {
                    stats.deletedCount++;
                    deletedRound++;
                }
            }
            if (deletedRound == 0) {
                // 本轮一个都没删掉，剩下的都是失败项，留给下轮任务重试
                stats.failureCount += content.size();
                break;
            }
        }
    }

    /**
     * 本轮清理的计数
     */
    private static class Stats {
        int tempCount;
        int orphanCount;
        int deletedCount;
        int failureCount;
    }
}

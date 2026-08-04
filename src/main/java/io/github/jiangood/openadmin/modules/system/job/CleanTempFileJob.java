package io.github.jiangood.openadmin.modules.system.job;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@DisallowConcurrentExecution
@JobDescription(label = "清理临时文件", params = {})
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
    public String execute(JobDataMap data, Logger logger) throws Exception {
        int cleanMinutes = systemProperties.getFile().getCleanUnclaimedMinutes();
        Date deadline = DateUtil.offsetMinute(new Date(), -cleanMinutes);

        // 1. 单条 UPDATE 标记超时未认领为待删除（TEMP -> PENDING_DELETE，无需查询）
        int unclaimedCount = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, deadline);

        // 2. 孤儿扫描：分页只读扫描，孤儿收集后统一标记待删除
        int orphanCount = markOrphans(logger);

        // 3. 删除待删文件：分页物理删除，扫描完成后批量删除库行
        int deletedCount = deletePendingFiles(logger);

        logger.info("临时文件清理完成，标记未认领 {} 个，标记孤儿 {} 个，删除待删 {} 个",
                unclaimedCount, orphanCount, deletedCount);
        return "清理完成，标记未认领 " + unclaimedCount + " 个，标记孤儿 " + orphanCount
                + " 个，删除待删 " + deletedCount + " 个";
    }

    /**
     * 分页扫描在用文件（只读，扫描时不改状态），业务记录已不存在则收集孤儿，扫描完统一标记
     */
    private int markOrphans(Logger logger) {
        List<String> orphanNames = new ArrayList<>();
        int page = 0;
        while (true) {
            List<SysFile> content = sysFileRepository.findByStatus(FileStatus.IN_USE, PageRequest.of(page, PAGE_SIZE)).getContent();
            if (content.isEmpty()) {
                break;
            }
            for (SysFile file : content) {
                try {
                    if (!jdbcRunner.existsById(file.getJoinTable(), file.getJoinId())) {
                        orphanNames.add(file.getObjectName());
                    }
                } catch (Exception e) {
                    logger.error("检查业务记录存在性失败: joinTable={}, joinId={}, error={}",
                            file.getJoinTable(), file.getJoinId(), e.getMessage());
                }
            }
            page++;
        }
        updateStatusBatch(orphanNames);
        return orphanNames.size();
    }

    /**
     * 分页删除待删文件：扫描时只做物理删除（不影响 DB 结果集），扫描完成后批量删除库行，
     * 避免边删边翻页导致行偏移漏删
     */
    private int deletePendingFiles(Logger logger) {
        List<String> deletedIds = new ArrayList<>();
        int page = 0;
        while (true) {
            List<SysFile> content = sysFileRepository.findByStatus(FileStatus.PENDING_DELETE, PageRequest.of(page, PAGE_SIZE)).getContent();
            if (content.isEmpty()) {
                break;
            }
            for (SysFile file : content) {
                if (sysFileService.deletePhysicalFile(file)) {
                    deletedIds.add(file.getId());
                }
            }
            page++;
        }
        for (int i = 0; i < deletedIds.size(); i += PAGE_SIZE) {
            List<String> batch = deletedIds.subList(i, Math.min(i + PAGE_SIZE, deletedIds.size()));
            try {
                sysFileRepository.deleteAllByIdInBatch(batch);
            } catch (Exception e) {
                logger.error("删除文件记录失败，保留待删除状态以便重试: count={}, error={}", batch.size(), e.getMessage());
            }
        }
        return deletedIds.size();
    }

    /**
     * 分批批量更新状态，避免单次 IN 条件过大
     */
    private void updateStatusBatch(List<String> objectNames) {
        for (int i = 0; i < objectNames.size(); i += PAGE_SIZE) {
            List<String> batch = objectNames.subList(i, Math.min(i + PAGE_SIZE, objectNames.size()));
            sysFileRepository.updateStatusByObjectNames(batch, FileStatus.PENDING_DELETE);
        }
    }
}

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

        // 1. 标记超时未认领为待删除（TEMP -> PENDING_DELETE）
        int unclaimedCount = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, deadline);

        // 2. 孤儿扫描：分页只读扫描，孤儿逐个标记待删除
        int orphanCount = markOrphans(logger);

        // 3. 删除待删文件：逐文件物理删除并删库行
        int deletedCount = deletePendingFiles(logger);

        logger.info("临时文件清理完成，标记未认领 {} 个，标记孤儿 {} 个，删除待删 {} 个",
                unclaimedCount, orphanCount, deletedCount);
        return "清理完成，标记未认领 " + unclaimedCount + " 个，标记孤儿 " + orphanCount
                + " 个，删除待删 " + deletedCount + " 个";
    }

    /**
     * 扫描在用文件，业务记录已不存在的逐个标记待删除（IN_USE -> PENDING_DELETE）
     */
    private int markOrphans(Logger logger) {
        int count = 0;
        for (SysFile file : sysFileRepository.findByStatus(FileStatus.IN_USE)) {
            try {
                if (!jdbcRunner.existsById(file.getJoinTable(), file.getJoinId())) {
                    sysFileRepository.updateStatusByObjectNames(List.of(file.getObjectName()), FileStatus.PENDING_DELETE);
                    count++;
                }
            } catch (Exception e) {
                logger.error("检查业务记录存在性失败: joinTable={}, joinId={}, error={}",
                        file.getJoinTable(), file.getJoinId(), e.getMessage());
            }
        }
        return count;
    }

    /**
     * 删除待删文件：反复取第一页（offset 固定 0，删掉的行让剩余记录前移），
     * 逐文件物理删除并删库行，失败的文件保留 PENDING_DELETE 待下轮重试
     */
    private int deletePendingFiles(Logger logger) {
        int count = 0;
        while (true) {
            List<SysFile> content = sysFileRepository.findByStatus(FileStatus.PENDING_DELETE, PageRequest.of(0, PAGE_SIZE)).getContent();
            if (content.isEmpty()) {
                break;
            }
            int deletedRound = 0;
            for (SysFile file : content) {
                if (sysFileService.deleteFileInternal(file)) {
                    count++;
                    deletedRound++;
                }
            }
            if (deletedRound == 0) {
                break;
            }
        }
        return count;
    }
}

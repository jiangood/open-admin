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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@DisallowConcurrentExecution
@JobDescription(label = "清理临时文件", params = {})
public class CleanTempFileJob extends BaseJob {

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

        // 1. 标记超时未认领为待删除
        List<SysFile> unclaimedFiles = sysFileRepository.findByStatusAndCreateTimeBefore(FileStatus.TEMP, deadline);
        logger.info("找到 {} 个过期临时文件 (clean={}m)", unclaimedFiles.size(), cleanMinutes);
        int unclaimedCount = markPendingDelete(unclaimedFiles);

        // 2. 孤儿扫描：业务记录已不存在则标记待删除
        List<String> orphanNames = new ArrayList<>();
        for (SysFile file : sysFileRepository.findByStatus(FileStatus.IN_USE)) {
            try {
                if (!jdbcRunner.existsById(file.getJoinTable(), file.getJoinId())) {
                    orphanNames.add(file.getObjectName());
                }
            } catch (Exception e) {
                logger.error("检查业务记录存在性失败: joinTable={}, joinId={}, error={}",
                        file.getJoinTable(), file.getJoinId(), e.getMessage());
            }
        }
        if (!orphanNames.isEmpty()) {
            sysFileRepository.updateStatusByObjectNames(orphanNames, FileStatus.PENDING_DELETE);
        }

        // 3. 最后删除所有待删除文件（含本轮标记与历史失败遗留）
        List<SysFile> pendingDeleteFiles = sysFileRepository.findByStatus(FileStatus.PENDING_DELETE);
        int deletedCount = 0;
        for (SysFile file : pendingDeleteFiles) {
            if (sysFileService.deleteFileInternal(file)) {
                deletedCount++;
            }
        }

        logger.info("临时文件清理完成，标记未认领 {} 个，标记孤儿 {} 个，删除待删 {} 个",
                unclaimedCount, orphanNames.size(), deletedCount);
        return "清理完成，标记未认领 " + unclaimedCount + " 个，标记孤儿 " + orphanNames.size()
                + " 个，删除待删 " + deletedCount + " 个";
    }

    private int markPendingDelete(List<SysFile> files) {
        List<String> names = new ArrayList<>();
        for (SysFile file : files) {
            names.add(file.getObjectName());
        }
        if (!names.isEmpty()) {
            sysFileRepository.updateStatusByObjectNames(names, FileStatus.PENDING_DELETE);
        }
        return names.size();
    }
}

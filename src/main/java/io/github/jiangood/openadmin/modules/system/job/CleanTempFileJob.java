package io.github.jiangood.openadmin.modules.system.job;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
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

        List<SysFile> unclaimedFiles = sysFileRepository.findUnclaimedFiles(deadline);
        logger.info("找到 {} 个过期临时文件 (clean={}m)", unclaimedFiles.size(), cleanMinutes);
        int unclaimedCount = deleteFiles(unclaimedFiles, logger);

        List<SysFile> orphans = new ArrayList<>();
        for (SysFile file : sysFileRepository.findClaimedFiles()) {
            try {
                if (!jdbcRunner.existsById(file.getJoinTable(), file.getJoinId())) {
                    orphans.add(file);
                }
            } catch (Exception e) {
                logger.error("检查业务记录存在性失败: joinTable={}, joinId={}, error={}",
                        file.getJoinTable(), file.getJoinId(), e.getMessage());
            }
        }
        int orphanCount = deleteFiles(orphans, logger);

        logger.info("临时文件清理完成，删除未认领 {} 个，孤儿文件 {} 个", unclaimedCount, orphanCount);
        return "清理完成，删除未认领 " + unclaimedCount + " 个，孤儿文件 " + orphanCount + " 个";
    }

    /**
     * 删除文件：先删物理文件（失败保留以便下轮重试），成功后再批量删除 DB 记录
     */
    private int deleteFiles(List<SysFile> files, Logger logger) {
        List<SysFile> removed = new ArrayList<>();
        for (SysFile file : files) {
            if (sysFileService.deletePhysicalFile(file)) {
                removed.add(file);
            }
        }
        if (!removed.isEmpty()) {
            try {
                sysFileRepository.deleteAllInBatch(removed);
            } catch (Exception e) {
                logger.error("删除文件记录失败: count={}, error={}", removed.size(), e.getMessage());
            }
        }
        return removed.size();
    }
}

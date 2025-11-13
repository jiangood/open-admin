package io.admin.modules.ureport.provider;

import cn.hutool.core.util.StrUtil;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DatabaseReportProvider implements ReportProvider {


    public static final String PREFIX = "db:";

    @Resource
    private UReportDao dao;


    @Override
    public String loadReport(String file) {
        UReport report = dao.findByFile(file);
        if (report != null) {
            return report.getContent();
        }
        return null;
    }

    @Override
    public void deleteReport(String file) {
        UReport report = dao.findByFile(file);
        if (report == null) {
            log.error("删除报表失败,报表不存在 {}", file);
        } else {
            dao.deleteById(report.getId());
        }

    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<UReport> list = dao.findAll();

        return list.stream().map(r -> {
            String file = r.getFile();

            String pureFile = StrUtil.removePrefix(file, getPrefix());


            return new ReportFile(pureFile, r.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @Override
    public void saveReport(String file, String content) {
        UReport r = dao.findByFile(file);
        if (r == null) {
            r = new UReport();
        }

        r.setFile(file);
        r.setContent(content);
        dao.save(r);
    }

    @Override
    public String getName() {
        return "数据库存储";
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}

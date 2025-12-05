package io.admin.modules.ureport.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.SpringUtils;
import io.admin.framework.config.security.HasPermission;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 简单示例
 */
@RequestMapping("admin/ureport")
@RestController
public class UReportController {


    @HasPermission("ureport:view")
    @RequestMapping("page")
    public AjaxResult page() {
        Collection<ReportProvider> list = SpringUtils.getBeans(ReportProvider.class);



        List<Dict> voList = new ArrayList<>();
        for (ReportProvider reportProvider : list) {
            List<ReportFile> reportFiles = reportProvider.getReportFiles();
            if(CollUtil.isEmpty(reportFiles)){
                continue;
            }
            for (ReportFile file : reportFiles) {
                Dict dict = new Dict();
                dict.put("providerName", reportProvider.getName());
                dict.put("providerPrefix",reportProvider.getPrefix());
                dict.put("name", file.getName());
                dict.put("updateDate", file.getUpdateDate());
                dict.put("previewUrl", "ureport/preview?_u=" + reportProvider.getPrefix() + file.getName());
                dict.put("designerUrl", "ureport/designer?_u=" + reportProvider.getPrefix() + file.getName());
                voList.add(dict);
            }
        }

        return AjaxResult.ok().data(new PageImpl<>(voList));
    }


}

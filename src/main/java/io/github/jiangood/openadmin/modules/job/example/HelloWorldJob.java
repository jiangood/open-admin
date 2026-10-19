package io.github.jiangood.openadmin.modules.job.example;

import io.github.jiangood.openadmin.util.field.FieldDescription;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import org.quartz.JobDataMap;
import org.slf4j.Logger;

@JobDescription(label = "你好世界", params = {@FieldDescription(name = "name", label = "姓名")})
public class HelloWorldJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        progress(0, "开始打招呼");
        logger.info("你好世界, 欢迎您{}!", data.get("name"));
        progress(50, "已打印问候语");
        logger.info("运行你好世界");
        progress(100, "完成");
        return "OK";
    }
}

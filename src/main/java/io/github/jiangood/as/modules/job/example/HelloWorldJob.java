package io.github.jiangood.as.modules.job.example;

import io.github.jiangood.as.common.tools.field.FieldDescription;
import io.github.jiangood.as.modules.job.BaseJob;
import io.github.jiangood.as.modules.job.JobDescription;
import org.quartz.JobDataMap;
import org.slf4j.Logger;

@JobDescription(label = "你好世界", params = {@FieldDescription(name = "name", label = "姓名")})
public class HelloWorldJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        System.out.printf("你好世界, 欢迎您%s!%n", data.get("name"));
        logger.info("运行你好世界");
        return "OK";
    }
}

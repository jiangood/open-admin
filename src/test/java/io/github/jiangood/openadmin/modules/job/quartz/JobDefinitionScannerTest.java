package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobDefinitionScannerTest {

    @Test
    void annotatedConcreteJobIsRegistrable() {
        assertTrue(JobDefinitionScanner.isRegistrable(AnnotatedJob.class));
    }

    @Test
    void plainBaseJobIsRegistrable() {
        assertTrue(JobDefinitionScanner.isRegistrable(PlainBaseJob.class));
    }

    @Test
    void unannotatedJobIsNotRegistrable() {
        assertFalse(JobDefinitionScanner.isRegistrable(PlainJob.class));
    }

    @Test
    void abstractAndInterfaceAreNotRegistrable() {
        assertFalse(JobDefinitionScanner.isRegistrable(AbstractAnnotatedJob.class));
        assertFalse(JobDefinitionScanner.isRegistrable(AnnotatedJobInterface.class));
    }

    @Test
    void nonJobSuffixIsStillRegistrableAtScanStage() {
        // 类名后缀在注册阶段校验，扫描阶段只负责找注解
        assertTrue(JobDefinitionScanner.isRegistrable(AnnotatedTask.class));
    }

    @JobDescription(label = "注解任务")
    static class AnnotatedJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    static class PlainJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "抽象任务")
    abstract static class AbstractAnnotatedJob implements Job {
    }

    @JobDescription(label = "接口任务")
    interface AnnotatedJobInterface extends Job {
    }

    @JobDescription(label = "后缀测试")
    static class AnnotatedTask implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    static class PlainBaseJob extends BaseJob {
        @Override
        public String execute(JobDataMap data, Logger logger) {
            return null;
        }
    }
}

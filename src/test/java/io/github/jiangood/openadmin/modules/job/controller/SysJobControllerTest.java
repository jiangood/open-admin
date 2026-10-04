package io.github.jiangood.openadmin.modules.job.controller;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.service.SysJobService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.field.Field;
import io.github.jiangood.openadmin.util.field.FieldDescription;
import io.github.jiangood.openadmin.util.field.ValueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysJobControllerTest {

    @Mock
    private SysJobService service;

    @Mock
    private QuartzManager quartzService;

    @InjectMocks
    private SysJobController controller;

    @io.github.jiangood.openadmin.modules.job.JobDescription(label = "测试任务", params = {
            @FieldDescription(name = "count", label = "数量", type = ValueType.NUMBER),
            @FieldDescription(name = "flag", label = "开关", type = ValueType.BOOLEAN),
            @FieldDescription(name = "name", label = "姓名", type = ValueType.STRING)
    })
    static class TestTypedJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            throw new UnsupportedOperationException("仅用于注解反射测试，不参与 Quartz 调度");
        }
    }

    @Test
    void triggerJob_shouldThrowWhenJobNotExists() throws Exception {
        when(service.findById("no-such-id")).thenReturn(Optional.empty());

        IdReq req = new IdReq();
        req.setId("no-such-id");

        assertThrows(IllegalArgumentException.class, () -> controller.triggerJob(req));
        verify(quartzService, never()).triggerJob(null);
    }

    @Test
    void triggerJob_shouldDelegateWhenJobExists() throws Exception {
        SysJob job = new SysJob();
        when(service.findById("job-1")).thenReturn(Optional.of(job));

        IdReq req = new IdReq();
        req.setId("job-1");

        controller.triggerJob(req);
        verify(quartzService).triggerJob(job);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getJobParamFields_shouldFillValueTypeFromFieldDescription() throws Exception {
        AjaxResult rs = controller.getJobParamFields(TestTypedJob.class.getName(), Map.of());

        List<Field> fields = (List<Field>) rs.getData();
        assertEquals(3, fields.size());
        assertEquals("number", fields.get(0).getValueType());
        assertEquals("boolean", fields.get(1).getValueType());
        assertEquals("string", fields.get(2).getValueType());
    }
}

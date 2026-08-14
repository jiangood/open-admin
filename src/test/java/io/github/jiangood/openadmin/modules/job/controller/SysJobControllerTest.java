package io.github.jiangood.openadmin.modules.job.controller;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.service.SysJobService;
import io.github.jiangood.openadmin.util.dto.IdReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
}

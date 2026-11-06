package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobAutoRegistrarTest {

    @Mock
    private SystemProperties systemProperties;

    @Mock
    private JobRegistrationManager registrationManager;

    private JobAutoRegistrar registrar;

    @BeforeEach
    void setUp() {
        registrar = new JobAutoRegistrar(systemProperties, registrationManager);
        lenient().when(systemProperties.isJobEnable()).thenReturn(true);
        lenient().when(systemProperties.isJobAutoRegister()).thenReturn(true);
    }

    @Test
    void delegatesToRegistrationManager() {
        when(registrationManager.sync()).thenReturn(new JobRegistrationManager.SyncResult(2, 1));

        registrar.run();

        verify(registrationManager).sync();
    }

    @Test
    void switchOffSkipsSync() {
        when(systemProperties.isJobAutoRegister()).thenReturn(false);

        registrar.run();

        verify(registrationManager, never()).sync();
    }

    @Test
    void jobModuleDisabledSkipsSync() {
        when(systemProperties.isJobEnable()).thenReturn(false);

        registrar.run();

        verify(registrationManager, never()).sync();
    }
}

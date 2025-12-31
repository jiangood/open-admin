package io.github.jiangood.sa.modules.system.service;

import io.github.jiangood.sa.framework.config.SysProperties;
import io.github.jiangood.sa.framework.config.data.SysMenuYmlDao;
import io.github.jiangood.sa.modules.system.dao.SysConfigDao;
import io.github.jiangood.sa.modules.system.entity.SysConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SysConfigServiceTest {

    @Mock
    private SysConfigDao sysConfigDao;

    @Mock
    private SysMenuYmlDao dataProp;

    @Mock
    private Environment env;

    @InjectMocks
    private SysConfigService sysConfigService;

    @Test
    public void testGetMixedFromDatabase() {
        String key = "test.key";
        String dbValue = "db.value";

        SysConfig config = new SysConfig();
        config.setCode(key);
        config.setValue(dbValue);

        when(sysConfigDao.findByCode(key)).thenReturn(config);

        String result = sysConfigService.getMixed(key, String.class);

        assertEquals(dbValue, result);
        verify(env).getProperty(key);
        verify(sysConfigDao).findByCode(key);
    }

    @Test
    public void testGetMixedFromEnvironment() {
        String key = "test.key";
        String envValue = "env.value";

        when(env.getProperty(key)).thenReturn(envValue);
        when(sysConfigDao.findByCode(key)).thenReturn(null);

        String result = sysConfigService.getMixed(key, String.class);

        assertEquals(envValue, result);
        verify(env).getProperty(key);
        verify(sysConfigDao).findByCode(key);
    }


    @Test
    public void testGetMixedReturnsNullForEmptyValue() {
        String key = "test.key";

        when(env.getProperty(key)).thenReturn(null);
        when(sysConfigDao.findByCode(key)).thenReturn(null);

        String result = sysConfigService.getMixed(key, String.class);

        assertNull(result);
        verify(env).getProperty(key);
        verify(sysConfigDao).findByCode(key);
    }

    @Test
    public void testGet() {
        String key = "test.key";
        String value = "test.value";

        SysConfig config = new SysConfig();
        config.setCode(key);
        config.setValue(value);

        when(sysConfigDao.findByCode(key)).thenReturn(config);

        String result = sysConfigService.get(key);

        assertEquals(value, result);
        verify(sysConfigDao).findByCode(key);
    }

    @Test
    public void testGetReturnsNullWhenNotFound() {
        String key = "test.key";

        when(sysConfigDao.findByCode(key)).thenReturn(null);

        String result = sysConfigService.get(key);

        assertNull(result);
        verify(sysConfigDao).findByCode(key);
    }

    @Test
    public void testGetBoolean() {
        String key = "boolean.key";

        SysConfig config = new SysConfig();
        config.setCode(key);
        config.setValue("true");

        when(sysConfigDao.findByCode(key)).thenReturn(config);

        boolean result = sysConfigService.getBoolean(key);

        assertTrue(result);
        verify(sysConfigDao).findByCode(key);
    }

    @Test
    public void testGetInt() {
        String key = "int.key";

        SysConfig config = new SysConfig();
        config.setCode(key);
        config.setValue("123");

        when(sysConfigDao.findByCode(key)).thenReturn(config);

        int result = sysConfigService.getInt(key);

        assertEquals(123, result);
        verify(sysConfigDao).findByCode(key);
    }
}

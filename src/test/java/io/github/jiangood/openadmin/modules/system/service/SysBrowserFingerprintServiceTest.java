package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.modules.system.entity.SysBrowserFingerprint;
import io.github.jiangood.openadmin.util.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class SysBrowserFingerprintServiceTest {

    @Autowired
    private SysBrowserFingerprintService service;

    @Test
    void saveRemark_shouldCreateUpdateAndClear() {
        SysBrowserFingerprint created = service.saveRemark("fp-1", " 张三的电脑 ", " 办公室 ");
        assertEquals("fp-1", created.getFingerprint());
        assertEquals("张三的电脑", created.getName());
        assertEquals("办公室", created.getRemark());

        SysBrowserFingerprint updated = service.saveRemark("fp-1", "李四的电脑", null);
        assertEquals(created.getId(), updated.getId());
        assertEquals("李四的电脑", updated.getName());
        assertNull(updated.getRemark());

        assertNull(service.saveRemark("fp-1", " ", ""));
        assertTrue(service.findMap(List.of("fp-1")).isEmpty());
    }

    @Test
    void findFingerprintsByRemark_shouldMatchNameAndRemark() {
        service.saveRemark("fp-alpha", "张三的办公电脑", "三楼东侧");
        service.saveRemark("fp-beta", "李四的笔记本", null);

        assertEquals(List.of("fp-alpha"), service.findFingerprintsByRemark("张三"));
        assertEquals(List.of("fp-alpha"), service.findFingerprintsByRemark("三楼"));
        assertEquals(List.of("fp-beta"), service.findFingerprintsByRemark("笔记本"));
        assertTrue(service.findFingerprintsByRemark("不存在").isEmpty());
    }

    @Test
    void findMap_shouldReturnRemarksForExistingFingerprintsOnly() {
        service.saveRemark("fp-1", "电脑A", null);
        service.saveRemark("fp-2", null, "备用设备");

        Map<String, SysBrowserFingerprint> result = service.findMap(List.of("fp-1", "fp-2", "fp-unknown"));

        assertEquals(2, result.size());
        assertEquals("电脑A", result.get("fp-1").getName());
        assertEquals("备用设备", result.get("fp-2").getRemark());
    }

    @Test
    void saveRemark_shouldRejectBlankFingerprint() {
        assertThrows(BusinessException.class, () -> service.saveRemark("  ", "电脑", null));
    }
}

package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DictSeedSyncIntegrationTest {

    @Autowired
    private SysDictTypeRepository typeRepository;
    @Autowired
    private SysDictItemRepository itemRepository;

    @Test
    void frameworkEnumsAreSyncedAsDicts() {
        assertTrue(typeRepository.findByTypeCode("approveStatus").isPresent());
        assertTrue(typeRepository.findByTypeCode("materialType").isPresent());
        assertTrue(typeRepository.findByTypeCode("fileStatus").isPresent());
    }

    @Test
    void sexDictMatchesEnumNoDrift() {
        List<SysDictItem> sexItems = itemRepository.findByTypeCode("sex");
        Map<String, String> labels = sexItems.stream()
                .collect(Collectors.toMap(SysDictItem::getCode, SysDictItem::getLabel));
        assertEquals(4, sexItems.size());
        assertEquals("男", labels.get("MALE"));
        assertEquals("女", labels.get("FEMALE"));
        assertEquals("未知", labels.get("UNKNOWN"));
        assertEquals("其他", labels.get("OTHER"));
    }

    @Test
    void approveStatusItemsKeepColors() {
        List<SysDictItem> items = itemRepository.findByTypeCode("approveStatus");
        assertEquals(4, items.size());
        Map<String, SysDictItem> byCode = items.stream()
                .collect(Collectors.toMap(SysDictItem::getCode, i -> i));
        assertEquals("审核通过", byCode.get("APPROVED").getLabel());
        assertEquals(StatusColor.SUCCESS, byCode.get("APPROVED").getColor());
    }
}

package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.ObjectProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictSeedSyncTest {

    @DictType(code = "testStatus", label = "测试状态")
    enum TestStatus {
        @DictItem(label = "待处理", color = "WARNING")
        PENDING,
        @DictItem(label = "已完成")
        DONE
    }

    @DictType(code = "testStatus", label = "测试状态")
    enum NoRemark {
        FOO
    }

    @Mock
    private SysDictTypeRepository typeRepository;
    @Mock
    private SysDictItemRepository itemRepository;
    @Mock
    private DictEnumScanner scanner;

    private DictSeedSync sync;

    @BeforeEach
    void setUp() {
        sync = new DictSeedSync(scanner, providerOf(typeRepository), providerOf(itemRepository));
        when(scanner.scan()).thenReturn(List.of(TestStatus.class));
    }

    private <T> ObjectProvider<T> providerOf(T object) {
        return new ObjectProvider<T>() {
            @Override
            public T getObject() {
                return object;
            }
        };
    }

    @Test
    void syncCreatesTypeAndItems() {
        when(typeRepository.findFirstByPidIsNull()).thenReturn(Optional.of(rootType("root")));
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.empty());
        when(typeRepository.save(any(SysDictType.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.findByTypeCodeAndCode(anyString(), anyString())).thenReturn(Optional.empty());
        when(itemRepository.save(any(SysDictItem.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.afterSeedDataInitialize();

        ArgumentCaptor<SysDictType> typeCaptor = ArgumentCaptor.forClass(SysDictType.class);
        verify(typeRepository).save(typeCaptor.capture());
        assertEquals("root", typeCaptor.getValue().getPid());
        assertEquals("testStatus", typeCaptor.getValue().getTypeCode());
        assertEquals("测试状态", typeCaptor.getValue().getTypeLabel());

        ArgumentCaptor<SysDictItem> itemCaptor = ArgumentCaptor.forClass(SysDictItem.class);
        verify(itemRepository, times(2)).save(itemCaptor.capture());
        List<SysDictItem> items = itemCaptor.getAllValues();
        assertEquals("PENDING", items.get(0).getCode());
        assertEquals("待处理", items.get(0).getLabel());
        assertEquals(StatusColor.WARNING, items.get(0).getColor());
        assertEquals(0, items.get(0).getSeq());
        assertEquals("DONE", items.get(1).getCode());
        assertNull(items.get(1).getColor());
        assertEquals(1, items.get(1).getSeq());
    }

    @Test
    void syncFixesLabelAndColorDrift() {
        when(typeRepository.findFirstByPidIsNull()).thenReturn(Optional.of(rootType("root")));
        SysDictType existingType = new SysDictType();
        existingType.setId("t1");
        existingType.setTypeCode("testStatus");
        existingType.setTypeLabel("旧名称");
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.of(existingType));

        SysDictItem staleItem = new SysDictItem();
        staleItem.setId("i1");
        staleItem.setTypeCode("testStatus");
        staleItem.setCode("PENDING");
        staleItem.setLabel("旧标签");
        staleItem.setColor(StatusColor.ERROR);
        staleItem.setEnabled(false);
        staleItem.setSeq(5);
        when(itemRepository.findByTypeCodeAndCode("testStatus", "PENDING")).thenReturn(Optional.of(staleItem));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "DONE")).thenReturn(Optional.empty());
        when(itemRepository.save(any(SysDictItem.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.afterSeedDataInitialize();

        verify(typeRepository).save(existingType);
        assertEquals("测试状态", existingType.getTypeLabel());
        assertEquals("待处理", staleItem.getLabel());
        assertEquals(StatusColor.WARNING, staleItem.getColor());
        assertTrue(staleItem.getEnabled());
        assertEquals(0, staleItem.getSeq());
    }

    @Test
    void syncIsIdempotent() {
        when(typeRepository.findFirstByPidIsNull()).thenReturn(Optional.of(rootType("root")));
        SysDictType existingType = new SysDictType();
        existingType.setId("t1");
        existingType.setTypeCode("testStatus");
        existingType.setTypeLabel("测试状态");
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.of(existingType));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "PENDING"))
                .thenReturn(Optional.of(item("PENDING", "待处理", StatusColor.WARNING, 0)));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "DONE"))
                .thenReturn(Optional.of(item("DONE", "已完成", null, 1)));

        sync.afterSeedDataInitialize();

        verify(typeRepository, never()).save(any(SysDictType.class));
        verify(itemRepository, never()).save(any(SysDictItem.class));
    }

    @Test
    void syncCreatesRootTypeWhenMissing() {
        when(typeRepository.findFirstByPidIsNull()).thenReturn(Optional.empty());
        when(typeRepository.save(any(SysDictType.class))).thenAnswer(inv -> {
            SysDictType saved = inv.getArgument(0);
            if (saved.getId() == null) {
                saved.setId("new-root");
            }
            return saved;
        });
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.empty());
        when(itemRepository.findByTypeCodeAndCode(anyString(), anyString())).thenReturn(Optional.empty());
        when(itemRepository.save(any(SysDictItem.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.afterSeedDataInitialize();

        ArgumentCaptor<SysDictType> typeCaptor = ArgumentCaptor.forClass(SysDictType.class);
        verify(typeRepository, times(2)).save(typeCaptor.capture());
        List<SysDictType> savedTypes = typeCaptor.getAllValues();
        assertEquals("new-root", savedTypes.get(0).getId());
        assertEquals("系统数据", savedTypes.get(0).getTypeLabel());
        assertEquals("new-root", savedTypes.get(1).getPid());
        assertEquals("testStatus", savedTypes.get(1).getTypeCode());
        assertEquals("测试状态", savedTypes.get(1).getTypeLabel());
    }

    @Test
    void syncFailsFastWhenDictItemMissing() {
        when(typeRepository.findFirstByPidIsNull()).thenReturn(Optional.of(rootType("root")));
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.empty());
        when(scanner.scan()).thenReturn(List.of(NoRemark.class));

        assertThrows(IllegalArgumentException.class, sync::afterSeedDataInitialize);
    }

    private SysDictType rootType(String id) {
        SysDictType root = new SysDictType();
        root.setId(id);
        return root;
    }

    private SysDictItem item(String code, String label, StatusColor color, int seq) {
        SysDictItem item = new SysDictItem();
        item.setTypeCode("testStatus");
        item.setCode(code);
        item.setLabel(label);
        item.setColor(color);
        item.setEnabled(true);
        item.setSeq(seq);
        return item;
    }
}

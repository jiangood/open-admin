package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.ApproveStatus;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
import io.github.jiangood.openadmin.framework.enums.Sex;
import io.github.jiangood.openadmin.framework.enums.YesNo;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictSeedSyncTest {

    // ---- 同步逻辑测试用夹具（嵌套枚举，不会被 scan() 发现） ----

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

    private DictSeedSync sync;

    @BeforeEach
    void setUp() {
        sync = new TestableDictSeedSync(List.of(TestStatus.class),
                providerOf(typeRepository), providerOf(itemRepository));
    }

    // 子类覆盖 scan()，隔离同步逻辑
    static class TestableDictSeedSync extends DictSeedSync {
        private final List<Class<? extends Enum<?>>> enums;

        TestableDictSeedSync(List<Class<? extends Enum<?>>> enums,
                             ObjectProvider<SysDictTypeRepository> typeProvider,
                             ObjectProvider<SysDictItemRepository> itemProvider) {
            super(blankBeanFactoryProvider(), typeProvider, itemProvider);
            this.enums = enums;
        }

        @Override
        public List<Class<? extends Enum<?>>> scan() {
            return enums;
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<BeanFactory> blankBeanFactoryProvider() {
        return mock(ObjectProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<BeanFactory> failingBeanFactoryProvider() {
        ObjectProvider<BeanFactory> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenThrow(new IllegalStateException("no AutoConfigurationPackages in unit test"));
        return provider;
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
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.empty());
        when(typeRepository.save(any(SysDictType.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.findByTypeCodeAndCode(anyString(), anyString())).thenReturn(Optional.empty());
        when(itemRepository.save(any(SysDictItem.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.afterSeedDataInitialize();

        ArgumentCaptor<SysDictType> typeCaptor = ArgumentCaptor.forClass(SysDictType.class);
        verify(typeRepository).save(typeCaptor.capture());
        assertEquals("1", typeCaptor.getValue().getPid());
        assertEquals("testStatus", typeCaptor.getValue().getTypeCode());
        assertEquals("测试状态", typeCaptor.getValue().getTypeLabel());

        ArgumentCaptor<SysDictItem> itemCaptor = ArgumentCaptor.forClass(SysDictItem.class);
        verify(itemRepository, times(2)).save(itemCaptor.capture());
        List<SysDictItem> items = itemCaptor.getAllValues();
        assertEquals("PENDING", items.get(0).getCode());
        assertEquals("待处理", items.get(0).getLabel());
        assertEquals("WARNING", items.get(0).getColor());
        assertEquals(0, items.get(0).getSeq());
        assertEquals("DONE", items.get(1).getCode());
        assertNull(items.get(1).getColor());
        assertEquals(1, items.get(1).getSeq());
    }

    @Test
    void syncFixesLabelAndColorDrift() {
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
        staleItem.setColor("ERROR");
        staleItem.setEnabled(false);
        staleItem.setSeq(5);
        when(itemRepository.findByTypeCodeAndCode("testStatus", "PENDING")).thenReturn(Optional.of(staleItem));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "DONE")).thenReturn(Optional.empty());
        when(itemRepository.save(any(SysDictItem.class))).thenAnswer(inv -> inv.getArgument(0));

        sync.afterSeedDataInitialize();

        verify(typeRepository).save(existingType);
        assertEquals("测试状态", existingType.getTypeLabel());
        assertEquals("待处理", staleItem.getLabel());
        assertEquals("WARNING", staleItem.getColor());
        assertTrue(staleItem.getEnabled());
        assertEquals(0, staleItem.getSeq());
    }

    @Test
    void syncIsIdempotent() {
        SysDictType existingType = new SysDictType();
        existingType.setId("t1");
        existingType.setTypeCode("testStatus");
        existingType.setTypeLabel("测试状态");
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.of(existingType));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "PENDING"))
                .thenReturn(Optional.of(item("PENDING", "待处理", "WARNING", 0)));
        when(itemRepository.findByTypeCodeAndCode("testStatus", "DONE"))
                .thenReturn(Optional.of(item("DONE", "已完成", null, 1)));

        sync.afterSeedDataInitialize();

        verify(typeRepository, never()).save(any(SysDictType.class));
        verify(itemRepository, never()).save(any(SysDictItem.class));
    }

    @Test
    void syncFailsFastWhenDictItemMissing() {
        DictSeedSync noRemarkSync = new TestableDictSeedSync(List.of(NoRemark.class),
                providerOf(typeRepository), providerOf(itemRepository));
        when(typeRepository.findByTypeCode("testStatus")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, noRemarkSync::afterSeedDataInitialize);
    }

    // ---- 自动发现（真实扫描，走 AutoConfigurationPackages 不可用的降级路径） ----

    @Test
    void scanFindsAllFrameworkEnums() {
        DictSeedSync real = new DictSeedSync(failingBeanFactoryProvider(),
                providerOf(typeRepository), providerOf(itemRepository));
        List<Class<? extends Enum<?>>> result = real.scan();
        assertTrue(result.contains(ApproveStatus.class));
        assertTrue(result.contains(Sex.class));
        assertTrue(result.contains(YesNo.class));
        assertTrue(result.contains(DataPermType.class));
        assertTrue(result.contains(ArticlePosition.class));
        assertTrue(result.contains(MaterialType.class));
        assertTrue(result.contains(FileStatus.class));
        assertTrue(result.stream().allMatch(Class::isEnum));
    }

    @Test
    void scanExcludesNonEnumDictTypeClasses() {
        DictSeedSync real = new DictSeedSync(failingBeanFactoryProvider(),
                providerOf(typeRepository), providerOf(itemRepository));
        List<Class<? extends Enum<?>>> result = real.scan();
        assertTrue(result.stream().noneMatch(c -> c.getName().contains("NotAnEnumDictType")));
    }

    @Test
    void scanExcludesNestedEnums() {
        DictSeedSync real = new DictSeedSync(failingBeanFactoryProvider(),
                providerOf(typeRepository), providerOf(itemRepository));
        List<Class<? extends Enum<?>>> result = real.scan();
        assertTrue(result.stream().noneMatch(c -> c.getName().contains("$")),
                "嵌套枚举（如 DictSeedSyncTest$TestStatus/$NoRemark 测试夹具）不应被当作字典枚举");
    }

    private SysDictItem item(String code, String label, String color, int seq) {
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

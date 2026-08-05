package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.ApproveStatus;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
import io.github.jiangood.openadmin.framework.enums.Sex;
import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.framework.enums.YesNo;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.enums.ArticlePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DictEnumScannerTest {

    private DictEnumScanner scanner;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ObjectProvider<BeanFactory> provider = (ObjectProvider<BeanFactory>) mock(ObjectProvider.class);
        when(provider.getObject()).thenThrow(new IllegalStateException("no AutoConfigurationPackages in unit test"));
        scanner = new DictEnumScanner(provider);
    }

    @Test
    void scanFindsAllFrameworkEnums() {
        List<Class<? extends Enum<?>>> result = scanner.scan();
        assertTrue(result.contains(ApproveStatus.class));
        assertTrue(result.contains(Sex.class));
        assertTrue(result.contains(YesNo.class));
        assertTrue(result.contains(DataPermType.class));
        assertTrue(result.contains(StatusColor.class));
        assertTrue(result.contains(ArticlePosition.class));
        assertTrue(result.contains(MaterialType.class));
        assertTrue(result.contains(FileStatus.class));
        assertTrue(result.stream().allMatch(Class::isEnum));
    }

    @Test
    void scanExcludesNonEnumDictTypeClasses() {
        List<Class<? extends Enum<?>>> result = scanner.scan();
        assertTrue(result.stream().noneMatch(c -> c.getName().contains("NotAnEnumDictType")));
    }
}

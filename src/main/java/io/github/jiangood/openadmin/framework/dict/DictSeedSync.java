package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.StatusColor;
import io.github.jiangood.openadmin.framework.spi.StartupHook;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import io.github.jiangood.openadmin.util.annotation.Remark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class DictSeedSync implements StartupHook {

    public static final String ROOT_TYPE_LABEL = "系统数据";

    private final DictEnumScanner scanner;
    private final ObjectProvider<SysDictTypeRepository> typeRepositoryProvider;
    private final ObjectProvider<SysDictItemRepository> itemRepositoryProvider;

    @Override
    @Transactional
    public void afterSeedDataInitialize() {
        List<Class<? extends Enum<?>>> enumClasses = scanner.scan();
        if (enumClasses.isEmpty()) {
            return;
        }
        SysDictTypeRepository typeRepository = typeRepositoryProvider.getObject();
        SysDictItemRepository itemRepository = itemRepositoryProvider.getObject();
        String rootId = ensureRootType(typeRepository);
        int typeCount = 0;
        int itemCount = 0;
        for (Class<? extends Enum<?>> enumClass : enumClasses) {
            DictType dictType = enumClass.getAnnotation(DictType.class);
            syncType(typeRepository, dictType, rootId);
            typeCount++;
            itemCount += syncItems(itemRepository, enumClass, dictType.code());
        }
        log.info("字典枚举同步完成：{} 个类型，{} 个字典项", typeCount, itemCount);
    }

    private String ensureRootType(SysDictTypeRepository typeRepository) {
        return typeRepository.findFirstByPidIsNull()
                .orElseGet(() -> {
                    SysDictType root = new SysDictType();
                    root.setTypeCode(null);
                    root.setTypeLabel(ROOT_TYPE_LABEL);
                    root.setEnabled(true);
                    root.setSeq(0);
                    return typeRepository.save(root);
                }).getId();
    }

    private void syncType(SysDictTypeRepository typeRepository, DictType dictType, String rootId) {
        typeRepository.findByTypeCode(dictType.code())
                .ifPresentOrElse(existing -> {
                    if (!dictType.label().equals(existing.getTypeLabel())) {
                        existing.setTypeLabel(dictType.label());
                        typeRepository.save(existing);
                    }
                }, () -> {
                    SysDictType type = new SysDictType();
                    type.setPid(rootId);
                    type.setTypeCode(dictType.code());
                    type.setTypeLabel(dictType.label());
                    type.setEnabled(true);
                    type.setSeq(0);
                    typeRepository.save(type);
                });
    }

    private int syncItems(SysDictItemRepository itemRepository, Class<? extends Enum<?>> enumClass, String typeCode) {
        int count = 0;
        Object[] constants = enumClass.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            int seq = i;
            Enum<?> constant = (Enum<?>) constants[i];
            String code = constant.name();
            String label = labelOf(constant);
            StatusColor color = colorOf(constant);
            itemRepository.findByTypeCodeAndCode(typeCode, code)
                    .ifPresentOrElse(item -> {
                        boolean changed = false;
                        if (!label.equals(item.getLabel())) { item.setLabel(label); changed = true; }
                        if (!Objects.equals(color, item.getColor())) { item.setColor(color); changed = true; }
                        if (!Boolean.TRUE.equals(item.getEnabled())) { item.setEnabled(true); changed = true; }
                        if (!Objects.equals(seq, item.getSeq())) { item.setSeq(seq); changed = true; }
                        if (changed) itemRepository.save(item);
                    }, () -> {
                        SysDictItem item = new SysDictItem();
                        item.setTypeCode(typeCode);
                        item.setCode(code);
                        item.setLabel(label);
                        item.setColor(color);
                        item.setEnabled(true);
                        item.setSeq(seq);
                        itemRepository.save(item);
                    });
            count++;
        }
        return count;
    }

    private String labelOf(Enum<?> constant) {
        try {
            Field field = constant.getDeclaringClass().getDeclaredField(constant.name());
            Remark remark = field.getAnnotation(Remark.class);
            if (remark == null) {
                throw new IllegalArgumentException(constant.getDeclaringClass().getSimpleName()
                        + "." + constant.name() + " 缺少 @Remark 注解");
            }
            return remark.value();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("读取枚举 @Remark 失败", e);
        }
    }

    private StatusColor colorOf(Enum<?> constant) {
        try {
            Field field = constant.getDeclaringClass().getDeclaredField(constant.name());
            DictColor dictColor = field.getAnnotation(DictColor.class);
            return dictColor == null ? null : dictColor.value();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("读取枚举 @DictColor 失败", e);
        }
    }
}

package io.github.jiangood.openadmin.modules.codegen.service;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.framework.file.FileField;
import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.FieldMetaVO;
import io.github.jiangood.openadmin.util.annotation.Remark;
import io.github.jiangood.openadmin.util.dto.Option;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 由实体类反射生成 {@link EntityMetaVO}。
 */
public final class EntityMetaFactory {

    public static final String CAT_STRING = "STRING";
    public static final String CAT_TEXT = "TEXT";
    public static final String CAT_NUMBER = "NUMBER";
    public static final String CAT_BOOLEAN = "BOOLEAN";
    public static final String CAT_DATE = "DATE";
    public static final String CAT_DATETIME = "DATETIME";
    public static final String CAT_ENUM = "ENUM";
    public static final String CAT_OTHER = "OTHER";

    private EntityMetaFactory() {
    }

    public static EntityMetaVO build(Class<?> entityClass) {
        EntityMetaVO meta = new EntityMetaVO();
        meta.setClassName(entityClass.getName());
        meta.setSimpleName(entityClass.getSimpleName());
        meta.setPackageName(entityClass.getPackageName());
        meta.setFrameworkEntity(entityClass.getName().startsWith("io.github.jiangood.openadmin"));
        meta.setLabel(labelOf(entityClass, entityClass.getSimpleName()));

        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && StringUtils.hasText(table.name())) {
            meta.setTableName(table.name());
        } else {
            meta.setTableName(toUnderline(entityClass.getSimpleName()));
        }

        meta.setModulePackage(modulePackageOf(entityClass.getPackageName()));
        meta.setModule(toKebab(entityClass.getSimpleName()));

        List<FieldMetaVO> fields = new ArrayList<>();
        for (Field field : collectFields(entityClass)) {
            FieldMetaVO fieldMeta = buildField(field);
            if (fieldMeta != null) {
                fields.add(fieldMeta);
            }
        }
        meta.setFields(fields);
        return meta;
    }

    /**
     * 收集实体自有字段：沿继承链向上直到 BaseEntity（不含），BaseEntity 的 id/审计字段不参与生成。
     */
    private static List<Field> collectFields(Class<?> entityClass) {
        List<Field> list = new ArrayList<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class && current != BaseEntity.class) {
            for (Field field : current.getDeclaredFields()) {
                list.add(field);
            }
            current = current.getSuperclass();
        }
        return list;
    }

    private static FieldMetaVO buildField(Field field) {
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isSynthetic()) {
            return null;
        }
        if (field.isAnnotationPresent(Transient.class)) {
            return null;
        }
        if (isAssociation(field)) {
            return null; // 关联字段需业务自定义处理，不自动生成
        }
        if ("serialVersionUID".equals(field.getName())) {
            return null;
        }

        Class<?> type = field.getType();
        FieldMetaVO meta = new FieldMetaVO();
        meta.setName(field.getName());
        meta.setType(type.getSimpleName());
        meta.setTypeFqn(type.getCanonicalName() != null ? type.getCanonicalName() : type.getName().replace('$', '.'));
        meta.setLabel(labelOf(field, humanize(field.getName())));
        meta.setRequired(isRequired(field));

        FileField fileField = field.getAnnotation(FileField.class);
        if (fileField != null) {
            meta.setFile(true);
            meta.setHtml(fileField.html());
        }

        String category = categoryOf(field, type);
        meta.setCategory(category);

        if (type.isEnum()) {
            DictType dictType = type.getAnnotation(DictType.class);
            if (dictType != null) {
                meta.setDictCode(dictType.code());
            } else {
                meta.setOptions(enumOptions(type));
            }
        }

        boolean searchable = !meta.isFile()
                && (CAT_STRING.equals(category) || CAT_ENUM.equals(category) || CAT_BOOLEAN.equals(category));
        meta.setSearchable(searchable);
        return meta;
    }

    private static String categoryOf(Field field, Class<?> type) {
        if (type == String.class) {
            if (field.isAnnotationPresent(Lob.class)) {
                return CAT_TEXT;
            }
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.columnDefinition().toUpperCase().contains("TEXT")) {
                return CAT_TEXT;
            }
            return CAT_STRING;
        }
        if (type == boolean.class || type == Boolean.class) {
            return CAT_BOOLEAN;
        }
        if (Number.class.isAssignableFrom(box(type))) {
            return CAT_NUMBER;
        }
        if (type == LocalDate.class) {
            return CAT_DATE;
        }
        if (type == LocalDateTime.class || type == java.util.Date.class
                || type == java.sql.Date.class || type == java.sql.Timestamp.class) {
            return CAT_DATETIME;
        }
        if (type.isEnum()) {
            return CAT_ENUM;
        }
        return CAT_OTHER;
    }

    private static boolean isAssociation(Field field) {
        return field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(ElementCollection.class)
                || field.isAnnotationPresent(Embedded.class);
    }

    private static boolean isRequired(Field field) {
        if (field.isAnnotationPresent(NotBlank.class)
                || field.isAnnotationPresent(NotNull.class)
                || field.isAnnotationPresent(NotEmpty.class)) {
            return true;
        }
        Column column = field.getAnnotation(Column.class);
        return column != null && !column.nullable();
    }

    private static List<Option> enumOptions(Class<?> enumType) {
        List<Option> options = new ArrayList<>();
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return options;
        }
        for (Object constant : constants) {
            Enum<?> item = (Enum<?>) constant;
            String label = item.name();
            try {
                Field field = enumType.getDeclaredField(item.name());
                DictItem dictItem = field.getAnnotation(DictItem.class);
                if (dictItem != null) {
                    label = dictItem.label();
                }
            } catch (NoSuchFieldException ignored) {
                // 枚举常量字段必定存在，忽略
            }
            options.add(new Option(item.name(), label));
        }
        return options;
    }

    private static String labelOf(AnnotatedElement element, String fallback) {
        Remark remark = element.getAnnotation(Remark.class);
        if (remark != null && StringUtils.hasText(remark.value())) {
            return remark.value();
        }
        return fallback;
    }

    /**
     * 无 @Remark 时把驼峰字段名转成可读标题，如 mainImage → Main Image。
     */
    private static String humanize(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(' ').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Class<?> box(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == short.class) return Short.class;
        if (type == byte.class) return Byte.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static String modulePackageOf(String packageName) {
        if (packageName.endsWith(".entity")) {
            return packageName.substring(0, packageName.length() - ".entity".length());
        }
        return packageName;
    }

    public static String toKebab(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

    private static String toUnderline(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}

package io.github.jiangood.openadmin.util.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@ToString
@Getter
@Slf4j
public class Table<T> {

    private final List<TableColumn<T>> columns = new ArrayList<>();
    private final List<T> dataSource;
    private Long totalElements;

    public Table(List<T> dataSource) {
        this.dataSource = dataSource;
    }

    public Table(Page<T> page) {
        this.dataSource = new ArrayList<>(page.getContent());
        this.totalElements = page.getTotalElements();
    }

    public static <T> Table<T> of(List<T> list, Class<T> cls) {
        Table<T> tb = new Table<>(list);

        boolean hasExcelAnn = Arrays.stream(cls.getDeclaredFields()).anyMatch(t -> t.isAnnotationPresent(Remark.class));
        if (hasExcelAnn) {
            addAnnotatedColumns(tb, cls);
        } else {
            log.warn("实体上未配置Excel注解，将使用默认导出");
            addDefaultColumns(tb, cls);
        }
        return tb;
    }

    private static <T> void addAnnotatedColumns(Table<T> tb, Class<T> cls) {
        for (Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(Remark.class) && isColumnType(f.getType())) {
                tb.addColumn(f.getAnnotation(Remark.class).value(), f.getName());
            }
        }
    }

    private static <T> void addDefaultColumns(Table<T> tb, Class<T> cls) {
        for (Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(Lob.class) || !isColumnType(f.getType())) {
                continue;
            }
            String title = f.isAnnotationPresent(Remark.class) ? f.getAnnotation(Remark.class).value() : f.getName();
            tb.addColumn(title, f.getName());
        }
    }

    private static boolean isColumnType(Class<?> type) {
        return type.isAssignableFrom(String.class) || type.isAssignableFrom(Number.class) || type.isAssignableFrom(LocalDateTime.class);
    }

    public TableColumn<T> addColumn(String title, String dataIndex) {
        TableColumn<T> column = new TableColumn<>(title, dataIndex);
        columns.add(column);
        return column;
    }

    public TableColumn<T> addColumn(String title, Function<T, Object> render) {
        TableColumn<T> column = new TableColumn<>(title, render);
        columns.add(column);
        return column;
    }

    @JsonIgnore
    public Object getColumnValue(TableColumn<T> col, T bean) {
        String dataIndex = col.getDataIndex();
        Function<T, Object> render = col.getRender();
        Object value = null;

        if (render != null) {
            value = render.apply(bean);
        } else if (dataIndex != null) {
            value = BeanUtil.getFieldValue(bean, dataIndex);
        }

        return value;
    }

    public String getColumnValueFormatted(TableColumn<T> col, T bean) {
        Object v = getColumnValue(col, bean);
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDateTime d) {
            return DateUtil.format(d, "yyyy-MM-dd HH:mm:ss");
        }

        return v.toString();
    }
}

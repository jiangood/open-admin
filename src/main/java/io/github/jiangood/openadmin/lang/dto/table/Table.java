package io.github.jiangood.openadmin.lang.dto.table;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.lang.annotation.Remark;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * 表格，用于导出，前端动态展示表格等
 * 参考了antd的格式
 * 前端也可以使用
 *
 * @param <T>
 */
@ToString
@Getter
@Slf4j
public class Table<T> {


    private final List<TableColumn<T>> columns = new ArrayList<>();

    private final List<T> dataSource;

    // 分页时，总数
    private Long totalElements;


    public Table(List<T> dataSource) {
        this.dataSource = dataSource;
    }

    public Table(Page<T> page) {
        // 传入的列表可能是不可改变的，这里需要
        this.dataSource = new ArrayList<>(page.getContent());
        this.totalElements = page.getTotalElements();
    }

    public static <T> Table<T> of(List<T> list, Class<T> cls) {
        Table<T> tb = new Table<>(list);


        boolean hasExcelAnn = Arrays.stream(cls.getDeclaredFields()).anyMatch(t -> t.isAnnotationPresent(Remark.class));
        if (hasExcelAnn) {
            for (Field f : cls.getDeclaredFields()) {
                if (!f.isAnnotationPresent(Remark.class)) {
                    continue;
                }

                Class<?> type1 = f.getType();
                if (type1.isAssignableFrom(String.class) || type1.isAssignableFrom(Number.class) || type1.isAssignableFrom(Date.class)) {
                    String title = f.getAnnotation(Remark.class).value();
                    tb.addColumn(title, f.getName());
                }
            }
            return tb;
        }


        log.warn("实体上未配置Excel注解，将使用默认导出");

        for (Field f : cls.getDeclaredFields()) {
            if (f.isAnnotationPresent(Lob.class)) {
                continue;
            }

            Class<?> type1 = f.getType();
            if (type1.isAssignableFrom(String.class) || type1.isAssignableFrom(Number.class) || type1.isAssignableFrom(Date.class)) {
                String title = f.isAnnotationPresent(Remark.class) ? f.getAnnotation(Remark.class).value() : f.getName();
                tb.addColumn(title, f.getName());
            }
        }
        return tb;
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

    /**
     * 获得用于渲染的字符串
     *
     * @param col
     * @param bean
     * @return
     */
    public String getColumnValueFormatted(TableColumn<T> col, T bean) {
        Object v = getColumnValue(col, bean);
        if (v == null) {
            return null;
        }
        if (v instanceof Date d) {
            return DateUtil.formatDateTime(d);
        }


        return v.toString();
    }


}

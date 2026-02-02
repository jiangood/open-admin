package io.github.jiangood.openadmin.lang.dto.table;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Function;

/**
 * title 必填， render和dataIndex二选一
 */
@Getter
@Setter
@Accessors(chain = true)
public class TableColumn<T> {

    String title;

    String dataIndex;

    Function<T, Object> render;

    Integer width;

    /**
     * 是否允许排序
     */
    Boolean sorter;


    public TableColumn() {
    }

    public TableColumn(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public TableColumn(String title, String dataIndex) {
        this.dataIndex = dataIndex;
        this.title = title;
    }

    public TableColumn(String title, Function<T, Object> render) {
        this.title = title;
        this.render = render;
    }

    public String getTitle() {
        return title == null ? dataIndex : title;
    }


}

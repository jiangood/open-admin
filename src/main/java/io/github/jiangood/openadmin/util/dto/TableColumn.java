package io.github.jiangood.openadmin.util.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Function;

@Getter
@Setter
@Accessors(chain = true)
public class TableColumn<T> {

    String title;
    String dataIndex;
    Function<T, Object> render;
    Integer width;
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

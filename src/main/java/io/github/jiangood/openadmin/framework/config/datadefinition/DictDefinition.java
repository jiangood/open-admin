package io.github.jiangood.openadmin.framework.config.datadefinition;

import io.github.jiangood.openadmin.framework.enums.StatusColor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DictDefinition {

    private String label;
    private String code;

    private List<Item> items = new ArrayList<>();



    @Data
    public static class Item {
        String label;
        String code;
        StatusColor color;

        /**
         * 是否启用， 禁用后，select等组件不能选择，但查看列表中可以查看
         */
        Boolean enabled = true;

        private Integer seq;
    }


}

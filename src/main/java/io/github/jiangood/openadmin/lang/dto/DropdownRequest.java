package io.github.jiangood.openadmin.lang.dto;

import lombok.Data;

import java.util.List;

/**
 * 下拉选择后台请求参数
 */
@Data
public class DropdownRequest {

    String searchText;

    /**
     * 默认选择的，
     * 方便返回到前端显示
     * 通常用于返回部分的情况
     */
    List<String> selected;


}

package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 系统字典树
 */
@Data
public class SysDictTreeVO {

    private String id;

    private String pid;

    private Object code;
    private String name;
    private String color;

    private List<SysDictTreeVO> children;

    private Boolean leaf;
}

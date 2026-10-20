package io.github.jiangood.openadmin.modules.codegen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 代码生成请求。
 */
@Data
public class CodegenReq {

    /** 实体类全限定名（必须命中扫描白名单） */
    @NotBlank(message = "请选择实体")
    private String className;

    /** 模块名，缺省取实体类简称 kebab-case */
    private String module;

    /** 中文名，缺省取实体 @Remark */
    private String label;

    /** 挂载的父菜单 id，缺省挂在系统管理下 */
    private String parentMenu = "sys";

    /** 要写入的文件（相对项目根路径，来自预览结果）；未包含的跳过，勾选即视为允许覆盖 */
    private List<String> paths;
}

package io.github.jiangood.openadmin.modules.codegen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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

    /** 目标文件已存在时是否覆盖 */
    private boolean overwrite;
}

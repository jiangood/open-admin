package io.github.jiangood.openadmin.modules.codegen.dto;

import lombok.Data;

/**
 * 单个待生成/已生成文件描述。
 */
@Data
public class GeneratedFileVO {

    /** 相对项目根目录的路径 */
    private String path;

    /** 绝对路径 */
    private String absolutePath;

    /** 文件内容 */
    private String content;

    /** 目标文件是否已存在 */
    private boolean exists;

    /** 文件类型：backend / frontend / menu */
    private String kind;
}

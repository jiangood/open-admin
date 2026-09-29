package io.github.jiangood.openadmin.modules.codegen.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成结果。
 */
@Data
public class CodegenResultVO {

    /** 全部文件（含未写入的） */
    private List<GeneratedFileVO> files = new ArrayList<>();

    /** 实际写入的文件（相对路径） */
    private List<String> written = new ArrayList<>();

    /** 因已存在且未开启覆盖而跳过的文件（相对路径） */
    private List<String> skipped = new ArrayList<>();
}

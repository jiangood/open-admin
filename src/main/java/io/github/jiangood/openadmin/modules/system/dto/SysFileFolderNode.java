package io.github.jiangood.openadmin.modules.system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理左侧文件夹树节点（按 objectName 的存储目录生成）
 */
@Data
@NoArgsConstructor
public class SysFileFolderNode {

    /**
     * 目录完整路径，如 public/img/202601
     */
    private String key;

    /**
     * 展示名称，如 公共文件 / 图片 / 2026年01月
     */
    private String title;

    /**
     * 该目录下（含子目录）的文件数量
     */
    private int count;

    private List<SysFileFolderNode> children = new ArrayList<>();

    public SysFileFolderNode(String key, String title) {
        this.key = key;
        this.title = title;
    }
}

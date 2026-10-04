package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

/**
 * 文件生命周期状态
 */
@DictType(code = "fileStatus", label = "文件状态")
public enum FileStatus {

    /**
     * 未认领（上传后默认）
     */
    @DictItem(label = "未认领", color = "DEFAULT")
    TEMP,

    /**
     * 使用中（已被业务记录认领）
     */
    @DictItem(label = "使用中", color = "SUCCESS")
    IN_USE,

    /**
     * 待删除（已标记，由清理任务统一删除）
     */
    @DictItem(label = "待删除", color = "ERROR")
    PENDING_DELETE
}

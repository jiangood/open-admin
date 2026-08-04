package io.github.jiangood.openadmin.framework.enums;

import io.github.jiangood.openadmin.util.annotation.Remark;

/**
 * 文件生命周期状态
 */
@Remark("文件状态")
public enum FileStatus {

    /**
     * 未认领（上传后默认）
     */
    @Remark("未认领")
    TEMP,

    /**
     * 使用中（已被业务记录认领）
     */
    @Remark("使用中")
    IN_USE,

    /**
     * 待删除（已标记，由清理任务统一删除）
     */
    @Remark("待删除")
    PENDING_DELETE
}

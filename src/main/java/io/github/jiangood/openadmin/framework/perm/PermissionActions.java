package io.github.jiangood.openadmin.framework.perm;

/**
 * 统一权限操作常量类
 * 提供标准化的操作动词，避免重复和歧义
 */
public class PermissionActions {

    // =============== 基础数据操作（CRUD） ===============
    /**
     * 新增/创建
     */
    public static final String CREATE = "create";
    /**
     * 查看/读取
     */
    public static final String READ = "read";
    /**
     * 修改/编辑
     */
    public static final String UPDATE = "update";
    /**
     * 删除
     */
    public static final String DELETE = "delete";

    /**
     * 新增或修改
     */
    public static final String CREATE_OR_UPDATE = "save";

    // =============== 扩展操作 ===============
    /**
     * 导出数据
     */
    public static final String EXPORT = "export";
    /**
     * 导入数据
     */
    public static final String IMPORT = "import";
    /**
     * 上传文件
     */
    public static final String UPLOAD = "upload";
    /**
     * 下载文件
     */
    public static final String DOWNLOAD = "download";

    // =============== 状态管理 ===============
    /**
     * 启用/激活
     */
    public static final String ENABLE = "enable";
    /**
     * 禁用/停用
     */
    public static final String DISABLE = "disable";
    /**
     * 发布
     */
    public static final String PUBLISH = "publish";
    /**
     * 撤销/取消发布
     */
    public static final String UNPUBLISH = "unpublish";

    // =============== 审批流程 ===============
    /**
     * 审批通过
     */
    public static final String APPROVE = "approve";
    /**
     * 审批驳回
     */
    public static final String REJECT = "reject";
    /**
     * 审核
     */
    public static final String AUDIT = "audit";
    /**
     * 确认
     */
    public static final String CONFIRM = "confirm";
    /**
     * 取消
     */
    public static final String CANCEL = "cancel";

    // =============== 权限分配 ===============
    /**
     * 分配/授权
     */
    public static final String ASSIGN = "assign";
    /**
     * 撤销权限
     */
    public static final String REVOKE = "revoke";

    // =============== 系统管理 ===============
    /**
     * 配置/设置
     */
    public static final String CONFIGURE = "configure";
    /**
     * 管理
     */
    public static final String MANAGE = "manage";
    /**
     * 重置
     */
    public static final String RESET = "reset";
    /**
     * 同步
     */
    public static final String SYNC = "sync";
    /**
     * 备份
     */
    public static final String BACKUP = "backup";
    /**
     * 恢复
     */
    public static final String RESTORE = "restore";

    // =============== 特殊业务操作 ===============
    /**
     * 打印
     */
    public static final String PRINT = "print";
    /**
     * 复制
     */
    public static final String COPY = "copy";
    /**
     * 移动
     */
    public static final String MOVE = "move";
    /**
     * 排序
     */
    public static final String SORT = "sort";


}
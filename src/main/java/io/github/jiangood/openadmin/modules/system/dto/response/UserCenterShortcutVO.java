package io.github.jiangood.openadmin.modules.system.dto.response;

import lombok.Data;

/**
 * 个人中心「快捷菜单」条目。
 */
@Data
public class UserCenterShortcutVO {

    /** 菜单 id */
    private String id;

    /** 菜单名称 */
    private String name;

    /** 页面路由路径 */
    private String path;

    /** Ant Design 图标组件名 */
    private String icon;

}

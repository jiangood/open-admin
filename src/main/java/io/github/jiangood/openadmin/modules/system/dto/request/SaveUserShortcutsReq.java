package io.github.jiangood.openadmin.modules.system.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 个人中心「快捷菜单」保存请求。
 */
@Data
public class SaveUserShortcutsReq {

    /**
     * 按展示顺序排列的菜单 id；空数组表示清空快捷菜单
     */
    private List<String> menuIds;

}

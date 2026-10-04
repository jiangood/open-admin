package io.github.jiangood.openadmin.modules.system.enums;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

@DictType(code = "articlePosition", label = "文章显示位置")
public enum ArticlePosition {

    @DictItem(label = "顶部导航-头像-下拉菜单")
    HEADER_AVATAR_DROPDOWN,

    @DictItem(label = "顶部导航-左侧")
    HEADER_LEFT,

    @DictItem(label = "顶部导航-右侧")
    HEADER_RIGHT,

    @DictItem(label = "不显示")
    NONE

}

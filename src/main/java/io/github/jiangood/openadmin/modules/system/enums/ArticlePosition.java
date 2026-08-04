package io.github.jiangood.openadmin.modules.system.enums;

import io.github.jiangood.openadmin.framework.dict.DictType;
import io.github.jiangood.openadmin.util.annotation.Remark;

@DictType(code = "articlePosition", label = "文章显示位置")
@Remark("文章显示位置")
public enum ArticlePosition {

    @Remark("顶部导航-头像-下拉菜单")
    HEADER_AVATAR_DROPDOWN,

    @Remark("顶部导航-左侧")
    HEADER_LEFT,

    @Remark("顶部导航-右侧")
    HEADER_RIGHT,

    @Remark("不显示")
    NONE

}

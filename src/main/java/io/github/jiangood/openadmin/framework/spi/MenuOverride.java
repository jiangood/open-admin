package io.github.jiangood.openadmin.framework.spi;

import java.util.Objects;

/**
 * 菜单覆盖项。
 * <p>
 * 框架内部数据结构，业务请通过 {@link MenuCustomization} 的
 * {@code rename}/{@code moveTo}/{@code order} 等方法表达修改意图。
 * 字段为 {@code null} 表示不修改。
 *
 * @param id   必填，要修改的菜单 id
 * @param name 新名称，{@code null} 表示不修改
 * @param pid  新父节点 id；{@code null} 表示不修改，空串表示移到根节点
 * @param seq  新排序值，{@code null} 表示不修改
 */
public record MenuOverride(String id, String name, String pid, Integer seq) {

    public MenuOverride {
        Objects.requireNonNull(id, "菜单 id 不能为空");
    }

    MenuOverride merge(MenuOverride other) {
        return new MenuOverride(id,
                other.name != null ? other.name : name,
                other.pid != null ? other.pid : pid,
                other.seq != null ? other.seq : seq);
    }
}

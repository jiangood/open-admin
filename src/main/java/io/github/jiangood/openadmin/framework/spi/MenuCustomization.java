package io.github.jiangood.openadmin.framework.spi;

import io.github.jiangood.openadmin.framework.config.MenuDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 菜单定制收集器。
 * <p>
 * 由框架在每次解析当前用户菜单时创建，业务在
 * {@link MenuCustomizer#customize(MenuCustomization)} 中调用本类的方法表达
 * 修改意图。只支持修改已有菜单，不支持新增或删除；隐藏菜单时其所有后代
 * 会一起隐藏。
 * <p>
 * 本对象仅在当前请求内有效，不要缓存引用。
 */
public class MenuCustomization {

    private final List<MenuDefinition> menus;
    private final Map<String, MenuOverride> overrides = new LinkedHashMap<>();
    private final List<RelativeMove> moves = new ArrayList<>();
    private final Set<String> hidden = new LinkedHashSet<>();

    public MenuCustomization(Collection<MenuDefinition> menus) {
        this.menus = List.copyOf(menus);
    }

    /**
     * 修改菜单名称。
     */
    public MenuCustomization rename(String id, String name) {
        return put(id, name, null, null);
    }

    /**
     * 把菜单挂到指定父节点下。
     * <p>父节点必须是当前用户可见的目录节点（非分隔线、无 path），否则忽略并 warn。
     */
    public MenuCustomization moveTo(String id, String pid) {
        return put(id, null, pid, null);
    }

    /**
     * 把菜单移到根节点。
     */
    public MenuCustomization moveToRoot(String id) {
        return put(id, null, "", null);
    }

    /**
     * 设置排序值，越小越靠前。最终顺序由框架统一保证。
     */
    public MenuCustomization order(String id, int seq) {
        return put(id, null, null, seq);
    }

    /**
     * 放到同级菜单 {@code targetId} 前面。
     * <p>目标菜单必须与当前菜单同父，跨父级请使用 {@link #moveTo(String, String)}。
     */
    public MenuCustomization moveBefore(String id, String targetId) {
        moves.add(new RelativeMove(requireId(id), requireId(targetId), true));
        return this;
    }

    /**
     * 放到同级菜单 {@code targetId} 后面。
     * <p>目标菜单必须与当前菜单同父，跨父级请使用 {@link #moveTo(String, String)}。
     */
    public MenuCustomization moveAfter(String id, String targetId) {
        moves.add(new RelativeMove(requireId(id), requireId(targetId), false));
        return this;
    }

    /**
     * 隐藏菜单及其所有后代。
     */
    public MenuCustomization hide(String id) {
        hidden.add(requireId(id));
        return this;
    }

    /**
     * 缩进格式的菜单树与待应用修改，便于日志排查。
     */
    public String dump() {
        StringBuilder sb = new StringBuilder("菜单:\n");
        Set<String> visited = new HashSet<>();
        List<MenuDefinition> roots = new ArrayList<>();
        Map<String, List<MenuDefinition>> children = new LinkedHashMap<>();
        Set<String> ids = new HashSet<>();

        for (MenuDefinition menu : menus) {
            ids.add(menu.getId());
        }
        for (MenuDefinition menu : menus) {
            String pid = blankToNull(menu.getPid());
            if (pid == null || !ids.contains(pid)) {
                roots.add(menu);
            } else {
                children.computeIfAbsent(pid, k -> new ArrayList<>()).add(menu);
            }
        }

        for (MenuDefinition root : roots) {
            dumpNode(root, 0, children, visited, sb);
        }
        for (MenuDefinition menu : menus) {
            if (!visited.contains(menu.getId())) {
                sb.append("[孤立/循环]");
                dumpNode(menu, 0, children, visited, sb);
            }
        }

        if (overrides.isEmpty() && moves.isEmpty() && hidden.isEmpty()) {
            return sb.toString();
        }

        sb.append("待应用:\n");
        for (MenuOverride override : overrides.values()) {
            sb.append("  覆盖 ").append(override.id());
            if (override.name() != null) {
                sb.append(" name=").append(override.name());
            }
            if (override.pid() != null) {
                sb.append(" pid=").append(override.pid().isEmpty() ? "<根>" : override.pid());
            }
            if (override.seq() != null) {
                sb.append(" seq=").append(override.seq());
            }
            sb.append('\n');
        }
        for (RelativeMove move : moves) {
            sb.append("  ").append(move.id())
                    .append(" 移动到 ").append(move.targetId())
                    .append(move.before() ? " 之前" : " 之后")
                    .append('\n');
        }
        if (!hidden.isEmpty()) {
            sb.append("  隐藏 ").append(hidden).append('\n');
        }
        return sb.toString();
    }

    private static void dumpNode(MenuDefinition menu, int depth,
                                 Map<String, List<MenuDefinition>> children,
                                 Set<String> visited, StringBuilder sb) {
        sb.append("  ".repeat(depth)).append("- ")
                .append(menu.getName() == null ? "" : menu.getName())
                .append(" [").append(menu.getId()).append("]")
                .append(" seq=").append(menu.getSeq());
        if (menu.isDivider()) {
            sb.append(" [分隔线]");
        }
        if (menu.getPath() != null && !menu.getPath().isBlank()) {
            sb.append(' ').append(menu.getPath());
        }
        sb.append('\n');

        if (!visited.add(menu.getId())) {
            return;
        }
        for (MenuDefinition child : children.getOrDefault(menu.getId(), List.of())) {
            dumpNode(child, depth + 1, children, visited, sb);
        }
    }

    public Collection<MenuOverride> getOverrides() {
        return List.copyOf(overrides.values());
    }

    public List<RelativeMove> getMoves() {
        return List.copyOf(moves);
    }

    public Set<String> getHidden() {
        return Collections.unmodifiableSet(hidden);
    }

    private MenuCustomization put(String id, String name, String pid, Integer seq) {
        requireId(id);
        overrides.merge(id, new MenuOverride(id, name, pid, seq), MenuOverride::merge);
        return this;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("菜单 id 不能为空");
        }
        return id;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * 相对位置调整。
     *
     * @param id       要移动的菜单 id
     * @param targetId 目标菜单 id
     * @param before   true 表示放到目标前面，false 表示放到目标后面
     */
    public record RelativeMove(String id, String targetId, boolean before) {
    }
}

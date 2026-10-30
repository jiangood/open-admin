package io.github.jiangood.openadmin.framework.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 内置头像定义。
 * <p>
 * 头像文件位于 {@code classpath:/static/avatars/}，随 jar 一起发布，可通过静态地址直接访问；
 * 接口 {@code GET admin/public/avatar-presets} 会返回完整列表，供 PC 端、移动端等客户端使用。
 */
public final class AvatarPresets {

    public static final String BASE_PATH = "/avatars";
    public static final String DEFAULT_FILE = "default.svg";

    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("preset:avatar-1", "鼠", "avatar-1.svg"),
            new Definition("preset:avatar-2", "牛", "avatar-2.svg"),
            new Definition("preset:avatar-3", "虎", "avatar-3.svg"),
            new Definition("preset:avatar-4", "兔", "avatar-4.svg"),
            new Definition("preset:avatar-5", "龙", "avatar-5.svg"),
            new Definition("preset:avatar-6", "蛇", "avatar-6.svg"),
            new Definition("preset:avatar-7", "马", "avatar-7.svg"),
            new Definition("preset:avatar-8", "羊", "avatar-8.svg"),
            new Definition("preset:avatar-9", "猴", "avatar-9.svg"),
            new Definition("preset:avatar-10", "鸡", "avatar-10.svg"),
            new Definition("preset:avatar-11", "狗", "avatar-11.svg"),
            new Definition("preset:avatar-12", "猪", "avatar-12.svg")
    );

    private AvatarPresets() {
    }

    /**
     * 默认头像地址（含 context-path）
     */
    public static String defaultUrl(String contextPath) {
        return contextPath + BASE_PATH + "/" + DEFAULT_FILE;
    }

    /**
     * 可选头像列表，第一项为默认头像（key 为空）
     */
    public static List<Item> list(String contextPath) {
        List<Item> list = new ArrayList<>(DEFINITIONS.size() + 1);
        list.add(new Item("", "默认头像", defaultUrl(contextPath)));
        for (Definition definition : DEFINITIONS) {
            list.add(new Item(definition.key(), definition.label(), contextPath + BASE_PATH + "/" + definition.file()));
        }
        return list;
    }

    private record Definition(String key, String label, String file) {
    }

    public record Item(String key, String label, String url) {
    }
}

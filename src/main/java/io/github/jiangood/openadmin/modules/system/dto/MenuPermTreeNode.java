package io.github.jiangood.openadmin.modules.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 角色授权用菜单权限树节点。
 * <p>
 * 由 {@code MenuDefinition} 转换而来，供角色权限设置的树表使用，
 * {@code permCodes}/{@code permNames} 一一对应。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuPermTreeNode {

    /** 菜单 id（树表 rowKey / 勾选 key） */
    private String id;

    /** 父菜单 id，为空表示根节点 */
    private String pid;

    /** 菜单显示名称 */
    private String name;

    /** 完整权限标识列表（格式：{id}:{code}） */
    private List<String> permCodes;

    /** 权限显示名称列表，与 permCodes 一一对应 */
    private List<String> permNames;

    /** 是否禁用 */
    private Boolean disabled;

    private List<MenuPermTreeNode> children;
}

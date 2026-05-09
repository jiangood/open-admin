package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/sysMenu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;


    @GetMapping("menu-tree")
    public AjaxResult menuTree() {
        return AjaxResult.ok().data(sysMenuService.menuTree());
    }


}

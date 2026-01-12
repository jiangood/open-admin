package io.github.jiangood.as.modules.system.controller;


import io.github.jiangood.as.common.dto.AjaxResult;
import io.github.jiangood.as.modules.system.service.SysMenuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/sysMenu")
public class SysMenuController {

    @Resource
    SysMenuService sysMenuService;


    @GetMapping("menuTree")
    public AjaxResult menuTree() {
        return AjaxResult.ok().data(sysMenuService.menuTree());
    }


}

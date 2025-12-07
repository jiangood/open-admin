package io.admin.modules.system.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysManual;
import io.admin.modules.system.service.SysManualService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("admin/sysManual")
public class SysManualPublicController {

    @Resource
    SysManualService service;



    /**
     * 用户点击帮助按钮
     *
     * @param searchText
     * @param pageable
     * @return
     */
    @RequestMapping("pageForUser")
    public AjaxResult pageForUser(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = {}) Pageable pageable) {
        Spec<SysManual> s = Spec.<SysManual>of().orLike(searchText, "name");

        // 查询并保留最大版本记录
        List<SysManual> list = service.findAll(s,Sort.by("name", "version"));
        Map<String,SysManual> rs = new HashMap<>();
        for (SysManual e : list) {
            if(!rs.containsKey(e.getName())){
                rs.put(e.getName(),e);
            }
        }
        Collection<SysManual> values = rs.values();

        return AjaxResult.ok().data(new PageImpl<>(new ArrayList<>(values)));
    }

}


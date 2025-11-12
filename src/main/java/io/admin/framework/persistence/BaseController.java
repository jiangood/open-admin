package io.admin.framework.persistence;

import io.admin.framework.data.service.BaseService;
import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.data.query.JpaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 排除了查询的基础控制器
 *
 * @param <T> 表示id为String的实体
 *
 * @deprecated 无法处理复杂情况下的参数解析
 **/
@Deprecated
public abstract class BaseController<T extends Persistable<String>> {

    @Autowired
    private BaseService<T> service;


    @Deprecated
    @RequestMapping("page")
    public AjaxResult page(   String searchText,
            @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        JpaQuery<T> q = new JpaQuery<>();


        Page<T> page = service.findAllByRequest(q, pageable);



        return AjaxResult.ok().data(page);
    }


    @PostMapping("save")
    public AjaxResult save(@RequestBody T input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }


    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }


}

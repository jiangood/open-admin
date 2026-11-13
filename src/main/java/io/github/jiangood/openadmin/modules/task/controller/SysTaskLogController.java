package io.github.jiangood.openadmin.modules.task.controller;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.task.entity.SysTaskLog;
import io.github.jiangood.openadmin.modules.task.registry.TaskLogRegistry;
import io.github.jiangood.openadmin.modules.task.service.SysTaskLogService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务中心：统一查看后台长任务的状态与日志。
 * <p>
 * 日志正文走 logviewer：{@code admin/sys/log/task/{logId}}，按任务记录的 permission 字段鉴权。
 * 本控制器的 {@code task:view} 控制列表页入口；{@code task:cancel} 控制取消操作。
 */
@RestController
@RequestMapping("admin/sys/task")
public class SysTaskLogController {

    @Resource
    private SysTaskLogService taskLogService;

    @Resource
    private TaskLogRegistry taskLogRegistry;

    /**
     * 任务记录（最新在前）。searchText 按标题模糊搜索，status / type 精确过滤。
     */
    @HasPermission("task:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, String status, String type,
                           @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Spec<SysTaskLog> q = Spec.of();
        q.orLike(searchText, SysTaskLog.Fields.title);
        if (StrUtil.isNotBlank(status)) {
            q.eq(SysTaskLog.Fields.status, status);
        }
        if (StrUtil.isNotBlank(type)) {
            q.eq(SysTaskLog.Fields.type, type);
        }
        Page<SysTaskLog> page = taskLogService.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    /**
     * 取消运行中的任务：关闭任务登记的可中断资源。
     * 任务线程随后自行失败并补全记录。当前阶段不可中断时返回失败。
     */
    @HasPermission("task:cancel")
    @PostMapping("cancel")
    public AjaxResult cancel(String logId) {
        Assert.hasText(logId, "logId 不能为空");
        SysTaskLog task = taskLogService.findByLogId(logId);
        Assert.notNull(task, "任务不存在");
        Assert.isTrue(task.isRunning(), "任务已结束");
        Assert.isTrue(taskLogRegistry.cancel(logId), "任务当前阶段不可取消，请稍后再试");
        return AjaxResult.ok().msg("已请求取消");
    }

    /**
     * 查询单条任务（供前端轮询运行状态/进度）。
     */
    @HasPermission("task:view")
    @RequestMapping("get")
    public AjaxResult get(String logId) {
        Assert.hasText(logId, "logId 不能为空");
        SysTaskLog task = taskLogService.findByLogId(logId);
        return AjaxResult.ok().data(task);
    }
}

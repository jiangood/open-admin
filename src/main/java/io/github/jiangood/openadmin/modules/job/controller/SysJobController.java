package io.github.jiangood.openadmin.modules.job.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import tools.jackson.core.JacksonException;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.util.field.Field;
import io.github.jiangood.openadmin.util.field.FieldDescription;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.log.OperationLogContext;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobParamFieldProvider;
import io.github.jiangood.openadmin.modules.job.dto.request.JobImportReq;
import io.github.jiangood.openadmin.modules.job.dto.request.JobReq;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.quartz.JobRegistrationManager;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.service.JobStatService;
import io.github.jiangood.openadmin.modules.job.service.SysJobService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.quartz.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

@RestController
@RequestMapping("admin/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService service;
    private final JobStatService statService;
    private final QuartzManager quartzService;
    private final JobRegistrationManager registrationManager;


    @HasPermission("job:read")
    @GetMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws SchedulerException {
        return AjaxResult.ok().data(service.page(searchText, pageable));
    }

    /**
     * 代码中存在、但数据库尚未注册的任务
     */
    @HasPermission("job:read")
    @GetMapping("unregistered")
    public AjaxResult unregistered() {
        return AjaxResult.ok().data(registrationManager.findUnregistered());
    }

    /**
     * 一键导入代码中的任务；keys 为空时导入全部未注册任务
     */
    @Log("定时任务-导入")
    @HasPermission("job:create")
    @PostMapping("import")
    public AjaxResult importJobs(@RequestBody(required = false) JobImportReq req) {
        int count = registrationManager.importJobs(req == null ? null : req.getKeys());
        return AjaxResult.ok().msg("成功导入 " + count + " 个任务").data(Map.of("count", count));
    }

    @Log(value = "定时任务-创建", detail = "创建任务「${#req.name}」")
    @HasPermission("job:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody JobReq req) throws Exception {
        SysJob param = BeanUtil.copyProperties(req, SysJob.class);
        validateJobClass(param.getJobClass());
        service.save(param, null);
        return AjaxResult.ok().msg("创建成功");
    }

    @Log(value = "定时任务-更新", detail = "修改任务「${#req.name ?: #req.id}」")
    @HasPermission("job:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody JobReq req, @RequestHeader("X-Body-Fields") List<String> updateFields) throws Exception {
        SysJob param = BeanUtil.copyProperties(req, SysJob.class);
        service.save(param, updateFields);
        return AjaxResult.ok().msg("更新成功");
    }


    @HasPermission("job:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) throws SchedulerException {
        service.deleteJob(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


    @Log("定时任务-执行一次")
    @HasPermission("job:trigger")
    @PostMapping("trigger-job")
    public AjaxResult triggerJob(@Valid @RequestBody IdReq req) throws SchedulerException, ClassNotFoundException {
        SysJob job = service.findById(req.getId()).orElse(null);
        Assert.notNull(job, "任务不存在，可能已被删除");
        OperationLogContext.setDetail("手动执行任务「" + job.getName() + "」");
        quartzService.triggerJob(job);

        return AjaxResult.ok().msg("执行一次命令已发送");
    }


    @GetMapping("job-class-options")
    public AjaxResult jobClassList() {
        Collection<String> basePackages = SpringTool.getBasePackageClasses().stream().map(Class::getPackageName).toList();

        Set<Class<?>> list = new HashSet<>();
        for (String basePackage : basePackages) {
            Set<Class<?>> list1 = ClassUtil.scanPackageBySuper(basePackage, Job.class);
            list.addAll(list1);
        }

        List<Option> options = list.stream()
                .filter(cls -> {
                    int mod = cls.getModifiers();
                    return !Modifier.isAbstract(mod) && !Modifier.isInterface(mod);
                })
                .map(cls -> {
                    String name = cls.getName();
                    String simpleName = cls.getSimpleName();

                    Option option = new Option();
                    option.setValue(name);
                    option.setLabel(simpleName);

                    JobDescription jobDesc = cls.getAnnotation(JobDescription.class);
                    if (jobDesc != null) {
                        option.setLabel(simpleName + "（" + jobDesc.label() + "）");
                    }

                    return option;
                }).toList();

        return AjaxResult.ok().data(options);
    }

    @PostMapping("get-job-param-fields")
    public AjaxResult getJobParamFields(String className, @RequestBody Map<String, Object> jobData) throws ClassNotFoundException, JacksonException {
        Class<?> jobCls = validateJobClass(className);
        String name = jobCls.getName();

        Option option = new Option();
        option.setValue(name);

        option.setLabel(name);


        List<Field> result = new ArrayList<>();
        JobDescription jobDesc = jobCls.getAnnotation(JobDescription.class);
        if (jobDesc != null) {
            option.setLabel(option.getLabel() + " " + jobDesc.label());

            FieldDescription[] params = jobDesc.params();
            for (FieldDescription param : params) {
                Field d = new Field();
                d.setName(param.name());
                d.setLabel(param.label());
                d.setRequired(param.required());
                d.setPlaceholder(param.placeholder());
                d.setDefaultValue(param.defaultValue());
                d.setValueType(param.type().name().toLowerCase());
                result.add(d);
            }

            Class<? extends JobParamFieldProvider> provider = jobDesc.paramsProvider();
            if (provider != null) {
                int mod = provider.getModifiers();
                boolean isInterface = Modifier.isInterface(mod);
                if (!isInterface) {
                    JobParamFieldProvider bean = SpringTool.getBean(provider);
                    List<Field> fields = bean.getFields(jobDesc, jobData);
                    result.addAll(fields);
                }
            }
        }


        return AjaxResult.ok().data(result);
    }

    @HasPermission("job:read")
    @GetMapping("execute-record")
    public AjaxResult executeRecordPage(@RequestParam String jobId, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysJobLog> q = Spec.of();
        q.eq(SysJobLog.Fields.sysJob + ".id", jobId);

        Page<SysJobLog> page = service.findAllExecuteRecord(q, pageable);
        return AjaxResult.ok().data(page);
    }


    @HasPermission("job:read")
    @GetMapping("progress")
    public AjaxResult allProgress() {
        return AjaxResult.ok().data(service.listProgress());
    }

    @HasPermission("job:read")
    @GetMapping("progress/{jobId}")
    public AjaxResult progress(@PathVariable String jobId) {
        return AjaxResult.ok().data(service.getProgress(jobId));
    }

    /**
     * 页面顶部卡片：运行中 / 今日执行 / 调度器信息
     */
    @HasPermission("job:read")
    @GetMapping("stat")
    public AjaxResult stat() {
        return AjaxResult.ok().data(statService.stat());
    }

    /**
     * 全部任务日志文件占用（字节数、文件数、告警阈值）
     */
    @HasPermission("job:read")
    @GetMapping("log-size")
    public AjaxResult totalLogSize() {
        return AjaxResult.ok().data(statService.totalLogSize());
    }

    /**
     * 指定任务的日志文件占用，用于「执行情况」弹窗
     */
    @HasPermission("job:read")
    @GetMapping("{jobId}/log-size")
    public AjaxResult jobLogSize(@PathVariable String jobId) {
        return AjaxResult.ok().data(statService.logSizeOfJob(jobId));
    }


    private static Class<?> validateJobClass(String className) throws ClassNotFoundException {
        Class<?> cls = Class.forName(className);
        if (!Job.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("类 " + className + " 未实现 org.quartz.Job 接口");
        }
        int mod = cls.getModifiers();
        if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
            throw new IllegalArgumentException("类 " + className + " 是抽象类或接口，不能作为定时任务");
        }
        return cls;
    }

    @ExceptionHandler(JobPersistenceException.class)
    public AjaxResult ex(JobPersistenceException e) {
        return AjaxResult.err().msg(e.getMessage());

    }

}

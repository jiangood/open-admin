package io.github.jiangood.openadmin.modules.job.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import tools.jackson.core.JacksonException;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.util.field.Field;
import io.github.jiangood.openadmin.util.field.FieldDescription;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobParamFieldProvider;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("admin/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService service;
    private final Scheduler scheduler;
    private final QuartzManager quartzService;


    @HasPermission("job:read")
    @GetMapping("page")
    public AjaxResult page(String name, String jobClass, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws SchedulerException {
        return AjaxResult.ok().data(service.page(name, jobClass, pageable));
    }

    @Log("定时任务-创建")
    @HasPermission("job:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody SysJob param) throws Exception {
        validateJobClass(param.getJobClass());
        service.save(param, null);
        return AjaxResult.ok().msg("创建成功");
    }

    @Log("定时任务-更新")
    @HasPermission("job:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody SysJob param, RequestBodyKeys updateFields) throws Exception {
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
                }).collect(Collectors.toList());

        return AjaxResult.ok().data(options);
    }

    @PostMapping("get-job-param-fields")
    public AjaxResult getJobParamFields(String className, @RequestBody Map<String, Object> jobData) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JacksonException {
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

    @GetMapping("execute-record")
    public AjaxResult executeRecordPage(@RequestParam String jobId, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysJobLog> q = Spec.of();
        q.eq(SysJobLog.Fields.sysJob + ".id", jobId);

        Page<SysJobLog> page = service.findAllExecuteRecord(q, pageable);
        return AjaxResult.ok().data(page);
    }


    @HasPermission("job:read")
    @GetMapping("status")
    public AjaxResult info() throws SchedulerException {
        SchedulerMetaData meta = scheduler.getMetaData();

        StringBuilder str = new StringBuilder("Quartz 调度器 (v");
        str.append(meta.getVersion());
        str.append(") '");
        str.append(meta.getSchedulerName());
        str.append("' 实例ID '");
        str.append(meta.getSchedulerInstanceId());
        str.append("'\n");
        str.append("  调度器类: '");
        str.append(meta.getSchedulerClass().getName());
        str.append("'");
        if (meta.isSchedulerRemote()) {
            str.append(" - 通过 RMI 访问.");
        } else {
            str.append(" - 本地运行.");
        }

        str.append("\n");
        if (!meta.isShutdown()) {
            if (meta.getRunningSince() != null) {
                str.append("  运行开始时间: ");
                str.append(DateUtil.formatDateTime(meta.getRunningSince()));
            } else {
                str.append("  尚未启动.");
            }

            str.append("\n");
            if (meta.isInStandbyMode()) {
                str.append("  当前处于待机模式.");
            } else {
                str.append("  当前不处于待机模式.");
            }
        } else {
            str.append("  调度器已被关闭.");
        }

        str.append("\n");
        str.append("  已执行任务数: ");
        str.append(meta.getNumberOfJobsExecuted());
        str.append("\n");
        str.append("  使用线程池 '");
        str.append(meta.getThreadPoolClass().getName());
        str.append("' - 线程数 ");
        str.append(meta.getThreadPoolSize());
        str.append(" 个.");
        str.append("\n");
        str.append("  使用作业存储 '");
        str.append(meta.getJobStoreClass().getName());
        str.append("' - ");
        if (meta.isJobStoreSupportsPersistence()) {
            str.append("支持持久化.");
        } else {
            str.append("不支持持久化.");
        }

        if (meta.isJobStoreClustered()) {
            str.append(" 并且是集群化的.");
        } else {
            str.append(" 并且不是集群化的.");
        }

        str.append("\n");
        return AjaxResult.ok().data(str.toString());
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

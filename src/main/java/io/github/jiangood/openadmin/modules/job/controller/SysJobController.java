package io.github.jiangood.openadmin.modules.job.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.antd.Option;
import io.github.jiangood.openadmin.lang.SpringTool;
import io.github.jiangood.openadmin.lang.field.Field;
import io.github.jiangood.openadmin.lang.field.FieldDescription;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobParamFieldProvider;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobExecuteRecord;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.service.SysJobService;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("admin/job")
public class SysJobController {

    @Resource
    private SysJobService service;

    @Resource
    private Scheduler scheduler;

    @Resource
    private QuartzManager quartzService;


    @HasPermission("job:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws SchedulerException {
        return AjaxResult.ok().data(service.page(searchText, pageable));
    }

    @PreAuthorize("hasAuthority('job:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysJob param, RequestBodyKeys updateFields) throws Exception {
        Class.forName(param.getJobClass());
        service.saveOrUpdateByUserAction(param, updateFields);
        return AjaxResult.ok().msg("操作成功");
    }


    @RequestMapping("delete")
    public AjaxResult delete(String id) throws SchedulerException {
        service.deleteJob(id);
        return AjaxResult.ok().msg("删除成功");
    }


    @Log("作业-执行一次")
    @PreAuthorize("hasAuthority('job:triggerJob')")
    @GetMapping("triggerJob")
    public AjaxResult triggerJob(String id) throws SchedulerException, ClassNotFoundException {
        SysJob job = service.findByRequest(id);
        quartzService.triggerJob(job);

        return AjaxResult.ok().msg("执行一次命令已发送");
    }


    @GetMapping("jobClassOptions")
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

                    Option option = new Option();
                    option.setValue(name);
                    option.setLabel(name);

                    JobDescription jobDesc = cls.getAnnotation(JobDescription.class);
                    if (jobDesc != null) {
                        option.setLabel(name + " " + jobDesc.label());
                    }

                    return option;
                }).collect(Collectors.toList());

        return AjaxResult.ok().data(options);
    }

    @PostMapping("getJobParamFields")
    public AjaxResult getJobParamFields(String className, @RequestBody Map<String, Object> jobData) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        Class<?> jobCls = Class.forName(className);
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

    @RequestMapping("executeRecord")
    public AjaxResult executeRecordPage(@RequestParam String jobId, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<SysJobExecuteRecord> q = Spec.of();
        q.eq(SysJobExecuteRecord.Fields.sysJob + ".id", jobId);

        Page<SysJobExecuteRecord> page = service.findAllExecuteRecord(q, pageable);
        return AjaxResult.ok().data(page);
    }


    @PreAuthorize("hasAuthority('job:view')")
    @RequestMapping("status")
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


    @ExceptionHandler(JobPersistenceException.class)
    public AjaxResult ex(JobPersistenceException e) {
        return AjaxResult.err().msg(e.getMessage());

    }

}

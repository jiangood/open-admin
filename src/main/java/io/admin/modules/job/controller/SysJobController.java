package io.admin.modules.job.controller;

import cn.hutool.core.util.ClassUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.admin.modules.job.entity.SysJob;
import io.admin.modules.job.entity.SysJobLog;
import io.admin.modules.job.quartz.QuartzManager;
import io.admin.modules.job.service.SysJobLogService;
import io.admin.modules.job.service.SysJobService;
import io.admin.framework.log.Log;
import io.admin.common.utils.field.Field;
import io.admin.common.utils.SpringTool;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.data.query.JpaQuery;
import io.admin.common.utils.field.FieldDescription;
import io.admin.common.dto.AjaxResult;
import io.admin.common.dto.antd.Option;
import io.admin.modules.job.JobDescription;
import io.admin.modules.job.JobParamFieldProvider;
import io.admin.framework.config.security.HasPermission;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @Resource
    private SysJobLogService sysJobLogService;



    @HasPermission("job:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws SchedulerException {
        JpaQuery<SysJob> q = new JpaQuery<>();
        q.searchText(searchText, SysJob.Fields.name, SysJob.Fields.jobClass);
        Page<SysJob> page = service.pageByRequest(q, pageable);

        List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
        Map<JobKey, JobExecutionContext> currentlyExecutingJobsMap = currentlyExecutingJobs.stream().collect(Collectors.toMap(ctx -> ctx.getJobDetail().getKey(), ctx -> ctx));


        for (SysJob job : page) {
            SysJobLog latest = sysJobLogService.findLatest(job);
            if (latest != null) {
                // TODO
                /*job.putExtData("beginTime", latest.getBeginTime());
                job.putExtData("endTime", latest.getEndTime());
                job.putExtData("jobRunTime", latest.getJobRunTimeLabel());
                job.putExtData("result", latest.getResult());*/
            }

            if (job.getEnabled()) {
                JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
                JobExecutionContext ctx = currentlyExecutingJobsMap.get(jobKey);
                if (ctx != null) {
                    // TODO
                  /*  job.putExtData("executing", true);
                    job.putExtData("fireTime", ctx.getFireTime());*/
                }
            }
        }


        return AjaxResult.ok().data(page);
    }

    @HasPermission("job:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysJob param, RequestBodyKeys updateFields) throws Exception {
        Class.forName(param.getJobClass());

        service.saveOrUpdateByRequest(param,updateFields);
        return AjaxResult.ok().msg("操作成功");
    }


    @RequestMapping("delete")
    public AjaxResult delete(String id) throws SchedulerException {
        service.deleteJob(id);
        return AjaxResult.ok().msg("删除成功");
    }


    @Log("作业-执行一次")
    @HasPermission("job:triggerJob")
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


    @ExceptionHandler(JobPersistenceException.class)
    public AjaxResult ex(JobPersistenceException e) {
        return AjaxResult.err().msg(e.getMessage());

    }

}

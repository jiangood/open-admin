package io.admin.framework.dict;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import io.admin.common.utils.SpringUtils;
import io.admin.common.utils.ann.Remark;
import io.admin.modules.system.dao.SysDictDao;
import io.admin.modules.system.dao.SysDictItemDao;
import io.admin.modules.system.entity.SysDict;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class DictAnnHandler {

    @Resource
    SysDictDao sysDictDao;

    @Resource
    SysDictItemDao sysDictItemDao;

    public void run() throws IllegalAccessException {
        log.info("开始解析字典注解");
        Set<Class<?>> classes = scanEnum();

        for (Class cls : classes) {
            Remark dictAnn = (Remark) cls.getAnnotation(Remark.class);

            String code = StrUtil.lowerFirst(cls.getSimpleName());
            String label = dictAnn.value();

            SysDict old = sysDictDao.findByIdOrCode(code);
            if (old != null) {
                sysDictItemDao.deleteByPid(old.getId());
                sysDictDao.deleteById(old.getId());
            }


            SysDict sysDict = sysDictDao.saveOrUpdate(code, label);

            Field[] fields = cls.getFields();
            for (int i = 0; i < fields.length; i++) {

                Field field = fields[i];
                String key = field.getName();
                Remark fieldAnnotation = field.getAnnotation(Remark.class);
                Assert.notNull(fieldAnnotation, "需要有" + Remark.class.getName() + "注解");

                String text = fieldAnnotation.value();



                sysDictItemDao.saveOrUpdate(sysDict, key, text, i, true);
            }
        }
    }


    private static Set<Class<?>> scanEnum() {
        Set<Class<?>> result = new HashSet<>();
        Set<Class<?>> all = SpringUtils.getBasePackageClasses();

        for (Class<?> superClass : all) {
            Set<Class<?>> set = ClassUtil.scanPackageByAnnotation(superClass.getPackageName(), Remark.class);
            for (Class<?> cls : set) {
                if (cls.isEnum()) {
                    result.add(cls);
                }
            }
        }
        return result;
    }


}

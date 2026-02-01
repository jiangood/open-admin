package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysDict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class SysDictDao extends BaseDao<SysDict> {


    @Transactional
    public SysDict add(String code, String text) {
        SysDict dict = new SysDict();
        dict.setCode(code);
        dict.setText(text);
        dict = this.save(dict);
        return dict;
    }

    public SysDict findByCode(String code) {
        return this.findByField(SysDict.Fields.code, code);
    }

    public boolean existsByCode(String code) {
        Spec<SysDict> spec = Spec.of();
        return this.exists(spec.eq("code", code));
    }

    public SysDict findByIdOrCode(String code) {
        Spec<SysDict> spec = Spec.of();
        spec.or(Spec.<SysDict>of().eq(SysDict.Fields.code, code),
                Spec.<SysDict>of().eq("id", code));

        return this.findOne(spec);
    }


    @Transactional
    public SysDict saveOrUpdate(String code, String label, boolean isNumber) {
        SysDict dict = this.findByCode(code);
        if (dict == null) {
            dict = new SysDict();
            dict.setCode(code);
        }
        dict.setText(label);
        dict.setIsNumber(isNumber);
        return this.save(dict);
    }
}

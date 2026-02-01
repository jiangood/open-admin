package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Multimap;
import io.github.jiangood.openadmin.lang.GoogleTool;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.dao.SysDictDao;
import io.github.jiangood.openadmin.modules.system.dao.SysDictItemDao;
import io.github.jiangood.openadmin.modules.system.entity.SysDict;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysDictService extends BaseService<SysDict> {

    @Resource
    private SysDictItemDao sysDictItemDao;

    @Resource
    private SysDictDao sysDictDao;

    public String findText(String typeCode, String itemCode) {
        SysDict dict = sysDictDao.findByCode(typeCode);
        SysDictItem item = sysDictItemDao.findByDictAndCode(dict, itemCode);
        if (item == null) {
            return null;
        }
        return item.getText();
    }

    /**
     * 初始一个数据字典
     * 先判断是否存在
     *
     * @param code
     * @param text
     * @return
     */
    @Transactional
    public SysDict init(String code, String text, String... itemCodeTextArr) {
        SysDict old = sysDictDao.findByCode(code);
        if (old != null) {
            log.info("字典已存在，忽略初始化。 {}={}", code, text);
            return old;
        }
        SysDict dict = new SysDict();
        dict.setCode(code);
        dict.setText(text);
        dict = sysDictDao.save(dict);

        for (int i = 0; i < itemCodeTextArr.length; i = i + 2) {
            String itemCode = itemCodeTextArr[i];
            String itemValue = itemCodeTextArr[i + 1];
            sysDictItemDao.add(dict, itemCode, itemValue);
        }

        return dict;
    }

    public Map<String, Collection<SimpleDictItem>> dictMap() {
        List<SysDictItem> list = sysDictItemDao.findAll(Sort.by(SysDictItem.Fields.seq));

        Multimap<String, SimpleDictItem> map = GoogleTool.newMultimap();

        for (SysDictItem item : list) {
            SysDict sysDict = item.getSysDict();
            String dictCode = sysDict.getCode();
            dictCode = StrUtil.toUnderlineCase(dictCode).toUpperCase();

            Boolean isNumber = sysDict.getIsNumber() != null ? sysDict.getIsNumber() : false;
            Object value = isNumber ? Integer.parseInt(item.getCode()) : item.getCode();

            map.put(dictCode, new SimpleDictItem(value, item.getText(), item.getColor()));
        }

        return map.asMap();
    }

    public record SimpleDictItem(Object value, String label, String color) {
    }

}

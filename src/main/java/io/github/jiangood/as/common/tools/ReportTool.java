package io.github.jiangood.as.common.tools;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计相关工具类
 */
public class ReportTool {


    /**
     * 列表累加
     *
     * @param list             数据
     * @param ignoreProperties 忽略的属性
     * @return 和
     * @throws InvocationTargetException 反射异常
     * @throws IllegalAccessException    反射异常
     */
    public static Map<String, BigDecimal> addAll(List<?> list, String... ignoreProperties) throws InvocationTargetException, IllegalAccessException {
        Map<String, BigDecimal> result = new HashMap<>();
        for (Object t : list) {
            Method[] methods = t.getClass().getMethods();
            for (Method m : methods) {
                if (!m.getName().startsWith("getDefinition")) {
                    continue;
                }
                Class<?> returnType = m.getReturnType();
                if (!Number.class.isAssignableFrom(returnType)) {
                    continue;
                }

                Object v = m.invoke(t);

                String name = StrUtil.removePreAndLowerFirst(m.getName(), "getDefinition");


                if (v != null) {
                    BigDecimal dec = new BigDecimal(v.toString());

                    if (result.containsKey(name)) {
                        BigDecimal oldV = result.get(name);
                        result.put(name, oldV.add(dec));
                    } else {
                        result.put(name, dec);
                    }
                }

            }

        }

        return result;

    }


}

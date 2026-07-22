package io.github.jiangood.openadmin.util;


import cn.hutool.core.collection.CollUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSON工具类，提供JSON处理相关的工具方法
 */
@Slf4j
public class JsonTool {

    // singleton ,as to initialize need much TIME
    private static final ObjectMapper om = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .build();

    /**
     * 将对象转换为指定类型
     *
     * @param fromValue 源对象
     * @param toValueType 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        ObjectMapper om = new ObjectMapper();
        return om.convertValue(fromValue, toValueType);
    }

    /**
     * 通过转换json实现对象拷贝
     *
     * @param bean 源对象
     * @param <T> 泛型类型
     * @return 拷贝后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T bean) {
        String json = toJsonQuietly(bean);
        Class<T> cls = (Class<T>) bean.getClass();
        return jsonToBeanQuietly(json, cls);
    }

    /**
     * 通过转换json实现列表拷贝
     *
     * @param list 源列表
     * @param <T> 泛型类型
     * @return 拷贝后的列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> clone(List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        String json = toJsonQuietly(list);
        Class<T> cls = (Class<T>) list.get(0).getClass();
        return jsonToBeanListQuietly(json, cls);
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param o 要转换的对象
     * @return JSON字符串
     * @throws JacksonException 如果转换失败
     */
    public static String toJson(Object o) throws JacksonException {
        if (o == null) {
            return null;
        }
        return om.writeValueAsString(o);
    }

    /**
     * 将对象转换为JSON字符串（静默模式，忽略异常）
     *
     * @param o 要转换的对象
     * @return JSON字符串，如果转换失败返回null
     */
    public static String toJsonQuietly(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return toJson(o);
        } catch (JacksonException e) {
            log.error("JSON序列化失败", e);
        }
        return null;
    }

    /**
     * 将对象转换为格式化的JSON字符串（静默模式，忽略异常）
     *
     * @param o 要转换的对象
     * @return 格式化的JSON字符串，如果转换失败返回null
     */
    public static String toPrettyJsonQuietly(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JacksonException e) {
            log.error("JSON格式化输出失败", e);
        }
        return null;
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json JSON字符串
     * @param cls 目标对象类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws IOException 如果转换失败
     */
    public static <T> T jsonToBean(String json, Class<T> cls) throws IOException {
        if (json == null) {
            return null;

        }

        return om.readValue(json, cls);
    }

    public static <T> T jsonToBean(byte[] json, Class<T> cls) throws IOException {
        if (json == null) {
            return null;

        }

        return om.readValue(json, cls);
    }

    /**
     * 将JSON字符串转换为对象（使用TypeReference）
     *
     * @param json JSON字符串
     * @param valueTypeRef 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象
     * @throws IOException 如果转换失败
     */
    public static <T> T jsonToBean(String json, TypeReference<T> valueTypeRef)
            throws IOException {
        if (json == null) {
            return null;

        }

        return om.readValue(json, valueTypeRef);
    }

    /**
     * 将JSON字符串转换为对象（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @param cls 目标对象类型
     * @param <T> 泛型类型
     * @return 转换后的对象，如果转换失败返回null
     */
    public static <T> T jsonToBeanQuietly(String json, Class<T> cls) {
        if (json == null) {
            return null;
        }
        try {
            return jsonToBean(json, cls);

        } catch (Exception e) {
            log.error("JSON转实体失败", e);
        }
        return null;
    }

    /**
     * 将JSON字符串转换为对象列表（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @param cls 目标对象类型
     * @param <T> 泛型类型
     * @return 转换后的对象列表，如果转换失败返回null
     */
    public static <T> List<T> jsonToBeanListQuietly(String json, Class<T> cls) {
        if (json == null) {
            return null;
        }
        try {
            ObjectMapper mapper = om;
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, cls);
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            log.error("JSON转实体列表失败", e);
        }
        return null;
    }

    /**
     * 将JSON字符串转换为列表（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @param <T> 泛型类型
     * @return 转换后的列表，如果转换失败返回null
     */
    public static <T> List<T> jsonToListQuietly(String json) {
        if (json == null) {
            return null;
        }
        try {
            ObjectMapper mapper = om;
            return mapper.readValue(json, List.class);
        } catch (Exception e) {
            log.error("JSON转列表失败", e);
        }
        return null;
    }

    /**
     * 将JSON字符串转换为Object对象（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @return 转换后的Object对象，如果转换失败返回null
     */
    public static Object jsonToBeanQuietly(String json) {
        if (json == null) {
            return null;
        }
        return jsonToBeanQuietly(json, Object.class);
    }

    /**
     * 将JSON字符串转换为Map对象（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @return 转换后的Map对象，如果转换失败返回空Map
     */
    public static Map<String, Object> jsonToMapQuietly(String json) {
        if (json != null && !json.isEmpty()) {
            try {
                return om.readValue(json, new TypeReference<HashMap<String, Object>>() {
                });
            } catch (Exception e) {
                log.error("JSON转Map失败", e);
            }
        }

        return new HashMap<>();
    }

    /**
     * 将JSON字符串转换为Map对象
     *
     * @param json JSON字符串
     * @return 转换后的Map对象，如果转换失败返回空Map
     * @throws IOException 如果转换失败
     */
    public static Map<String, Object> jsonToMap(String json)
            throws IOException {
        if (json != null && !json.isEmpty()) {
            return om.readValue(json, new TypeReference<HashMap<String, Object>>() {
            });
        }
        return new HashMap<>();
    }

    /**
     * 将JSON字符串转换为String类型的Map对象（静默模式，忽略异常）
     *
     * @param json JSON字符串
     * @return 转换后的Map对象，如果转换失败返回null
     */
    public static Map<String, String> jsonToMapStringStringQuietly(String json) {
        if (json == null) {
            return null;
        }
        try {
            return om.readValue(json, new TypeReference<HashMap<String, String>>() {
            });
        } catch (JacksonException e) {
            log.error("JSON转Map<String,String>失败", e);
        }
        return null;
    }

    /**
     * 将JSON字符串解析为JsonNode对象
     *
     * @param json JSON字符串
     * @return JsonNode对象
     * @throws JacksonException 如果解析失败
     */
    public static JsonNode readTree(String json) throws JacksonException {
        JsonNode node = om.reader().readTree(json);
        return node;
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return om;
    }

}

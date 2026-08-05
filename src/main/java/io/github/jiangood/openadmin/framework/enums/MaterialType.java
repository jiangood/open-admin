package io.github.jiangood.openadmin.framework.enums;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.util.LinkedHashMap;

/**
 * 素材类型
 */
@DictType(code = "materialType", label = "素材类型")
public enum MaterialType {


    @DictItem(label = "文档")
    DOCUMENT,

    @DictItem(label = "图片")
    IMAGE,


    @DictItem(label = "视频")
    VIDEO,

    @DictItem(label = "音频")
    AUDIO;


    public static MaterialType parseBySuffix(String suffix) {
        MediaType mediaType = MediaTypeFactory.getMediaType("." + suffix).orElse(null);
        if (mediaType != null) {
            String type = mediaType.getType();
            LinkedHashMap<String, MaterialType> enumMap = EnumUtil.getEnumMap(MaterialType.class);
            MaterialType fileType = enumMap.get(type.toUpperCase());
            if (fileType != null) {
                return fileType;
            }
            if (type.contains("text")) {
                return MaterialType.DOCUMENT;
            }
        }

        if (StrUtil.equalsAnyIgnoreCase(suffix, "pdf", "doc", "ppt", "excel", "txt", "log")) {
            return MaterialType.DOCUMENT;
        }


        return null;
    }


}

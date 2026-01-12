package io.github.jiangood.as.common.tools.enums;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.as.common.tools.annotation.Remark;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.util.LinkedHashMap;

/**
 * 素材类型
 */
@Remark("materialType")
public enum MaterialType {


    @Remark("文档")
    DOCUMENT,

    @Remark("图片")
    IMAGE,


    @Remark("视频")
    VIDEO,

    @Remark("音频")
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

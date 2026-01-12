package io.github.jiangood.as.modules.system.entity;

import cn.hutool.core.io.FileUtil;
import io.github.jiangood.as.common.tools.ContentTypeTool;
import io.github.jiangood.as.common.tools.RequestTool;
import io.github.jiangood.as.common.tools.enums.MaterialType;
import io.github.jiangood.as.framework.data.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.InputStream;

/**
 * 文件信息
 */
@Getter
@Setter
@Entity
@FieldNameConstants
public class SysFile extends BaseEntity {

    /**
     * 交易号，由业务方指定
     */
    @Column(length = 32)
    private String tradeNo;
    /**
     * 文件名称（上传时候的文件名）
     */
    @Column(name = "file_origin_name", length = 100)
    private String originName;
    /**
     * 存储到bucket的名称, 支持目录， 如 2024/abc.jpg
     */
    @NotNull
    @Column(name = "file_object_name")
    private String objectName;
    /**
     * 文件后缀
     */
    @Column(name = "file_suffix", length = 10)
    private String suffix;
    @Column(name = "file_size")
    private Long size;
    @Column(length = 50)
    private String mimeType;
    @Enumerated(EnumType.STRING)
    private MaterialType type;
    private String title;
    private String description;
    private String hash;
    /**
     * 原始路径，针对那种互联网地址上传的
     */
    private String origUrl;
    // 预留字段
    private String extra1;
    private String extra2;
    private String extra3;
    @Transient
    private InputStream inputStream;

    public SysFile() {
    }

    public SysFile(String id) {
        this.setId(id);
    }

    @Transient
    public String getName() {
        return originName;
    }

    @Transient
    public String getSizeInfo() {
        if (size != null) {
            return FileUtil.readableFileSize(size);
        }
        return null;
    }

    @Transient
    public String getContentType() {
        return ContentTypeTool.getContentTypeByExtension(getSuffix());
    }

    @Transient
    public String getUrl() {
        HttpServletRequest request = RequestTool.currentRequest();
        if (request != null) {
            String baseUrl = RequestTool.getBaseUrl(request);
            return baseUrl + "/sysFile/preview/" + getId();
        }

        return null;
    }


}

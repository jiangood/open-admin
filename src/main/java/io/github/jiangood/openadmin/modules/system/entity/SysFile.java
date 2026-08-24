package io.github.jiangood.openadmin.modules.system.entity;

import cn.hutool.core.io.FileUtil;
import io.github.jiangood.openadmin.util.ContentTypeTool;
import io.github.jiangood.openadmin.util.RequestTool;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.SysFileConstants;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
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
@Table(name = "sys_file", indexes = {
        @Index(name = "idx_sys_file_unclaimed", columnList = "join_table, join_id, create_time")
})
@FieldNameConstants
public class SysFile extends BaseEntity { // NOSONAR: 实体以 id 为业务键，继承的 equals 即按 id 比较

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
    /**
     * 素材类型（字符串，图片上传时为 image，普通文件留空）
     */
    @Column(length = 50)
    private String type;
    /**
     * 生命周期状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FileStatus status;
    private String title;
    private String description;
    private String hash;
    /**
     * 原始路径，针对那种互联网地址上传的
     */
    private String origUrl;
    /**
     * 关联表名
     */
    @Column(length = 100)
    private String joinTable;

    /**
     * 关联记录ID
     */
    @Column(length = 32)
    private String joinId;
    // 预留字段
    private String extra1;
    private String extra2;
    private String extra3;
    @Transient
    private transient InputStream inputStream;
    /**
     * 上传者姓名（查询时由 createUser 关联 SysUser 填充，不落库）
     */
    @Transient
    private String createUserLabel;

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
            return baseUrl + request.getContextPath()
                    + SysFileConstants.FILE_URL_PATTERN.replace("{objectName}", getObjectName());
        }

        return null;
    }

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = FileStatus.TEMP;
        }
    }
}

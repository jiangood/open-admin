package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import io.github.jiangood.openadmin.util.DownloadTool;
import io.github.jiangood.openadmin.util.IdTool;
import io.github.jiangood.openadmin.util.ImgTool;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.file.FileOperator;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 文件服务类
 * <p>
 * 由于会被其他模块使用，不继承BaseService,减少干扰
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysFileService {

    public static final String PREVIEW_URL_PATTERN = "/preview/{id}";
    public static final String DOWNLOAD_URL_PATTERN = "/sysFile/download/{id}";


    public static final int[] IMAGE_SIZE = {400, 800, 1200};
    public static final String[] IMAGE_SIZE_LABEL = {"小图", "中图", "大图"};

    private final SystemProperties systemProperties;
    private final FileOperator fileOperator;
    private final SysFileRepository sysFileRepository;

    public Optional<SysFile> findByTradeNo(String tradeNo) {
        return sysFileRepository.findByTradeNo(tradeNo);
    }

    public String getPreviewUrl(String id, HttpServletRequest request) {
        String baseUrl = systemProperties.getBaseUrl();

        return baseUrl + getPreviewUrl(id);
    }

    /**
     * 获得预览相对url
     *
     * @param fileId
     * @return
     */
    public String getPreviewUrl(String fileId) {
        return PREVIEW_URL_PATTERN.replace("{id}", fileId);
    }

    public String getDownloadUrl(String fileId, HttpServletRequest request) {
        String baseUrl = systemProperties.getBaseUrl();

        return baseUrl + DOWNLOAD_URL_PATTERN.replace("{id}", fileId);
    }

    public void deleteById(String id) throws Exception {
        SysFile sysFile = sysFileRepository.findById(id).orElse(null);
        sysFileRepository.deleteById(id);

        // 删除原图
        fileOperator.delete(sysFile.getObjectName());
        // 删除已缓存的缩略图
        if (sysFile.getType() == MaterialType.IMAGE) {
            for (int size : IMAGE_SIZE) {
                String thumbName = getObjectName(sysFile, size);
                try {
                    fileOperator.delete(thumbName);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public SysFile uploadFile(byte[] data, String originalFilename) throws Exception {
        return this.uploadFile(new ByteArrayInputStream(data), originalFilename, data.length);
    }

    /**
     * 上传网络文件
     *
     * @param origUrl
     * @return
     * @throws Exception
     */
    public SysFile uploadWebFile(String origUrl, String tradeNo) throws Exception {
        log.info("准备上传网络文件 {}", origUrl);
        File tempFile = new File(FileUtil.getTmpDir(), FileNameUtil.mainName(origUrl));


        long size = HttpUtil.downloadFile(origUrl, tempFile);
        log.info("下载文件完成 {}", FileUtil.readableFileSize(size));

        String suffix = FileNameUtil.getSuffix(origUrl);
        if (StrUtil.isEmpty(suffix)) {
            suffix = FileTypeUtil.getType(tempFile);
            tempFile = FileUtil.rename(tempFile, tempFile.getName() + "." + suffix, true);
        }


        SysFile sysFile = this.uploadFile(tempFile, tradeNo);
        FileUtil.del(tempFile);

        sysFile.setOrigUrl(origUrl);
        sysFileRepository.save(sysFile);

        return sysFile;
    }

    public SysFile uploadFile(File file) throws Exception {
        return this.uploadFile(file, null);
    }

    public SysFile uploadFile(File file, String tradeNo) throws Exception {
        // 特殊处理后缀，如临时文件
        String suffix = FileNameUtil.getSuffix(file);
        if (StrUtil.isEmpty(suffix) || "tmp".equals(suffix)) {
            suffix = FileTypeUtil.getType(file, true);
        }

        String name = FileNameUtil.mainName(file) + "." + suffix;
        try (InputStream is = new FileInputStream(file)) {
            return this.uploadFile(is, name, file.length(), tradeNo);
        }
    }

    public SysFile uploadFile(MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        String name = file.getOriginalFilename();
        return this.uploadFile(is, name, file.getSize());
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size) throws Exception {
        return this.uploadFile(is, originalFilename, size, null);
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size, String tradeNo) throws Exception {
        log.info("上传文件:{} 大小:{}", originalFilename, FileUtil.readableFileSize(size));

        // 获取文件后缀并校验真实类型
        String suffix = null;
        if (ObjectUtil.isNotEmpty(originalFilename)) {
            suffix = StrUtil.subAfter(originalFilename, ".", true);
        }

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(64);
        String magicType = FileTypeUtil.getType(is);
        is.reset();

        // 始终用 magic byte 校验：阻断可执行文件伪装成普通图片/文档
        if (StrUtil.isNotEmpty(magicType) && isBlockedMagicType(magicType)) {
            throw new IllegalArgumentException("文件类型" + magicType + "不允许上传");
        }

        // WebP 文件：ImageIO 不支持缩略图生成，预览时自动回退原图
        if ("webp".equals(magicType)) {
            log.warn("上传文件真实类型为 webp，不受 ImageIO 支持，缩略图生成将回退原图");
            if (StrUtil.isNotEmpty(suffix) && !"webp".equalsIgnoreCase(suffix)) {
                log.info("文件后缀修正: {} -> webp (文件头检测)", suffix);
                suffix = "webp";
            }
        }

        if (StrUtil.isEmpty(suffix) && StrUtil.isNotEmpty(magicType)) {
            suffix = magicType;
            originalFilename += '.' + suffix;
        }

        Assert.hasText(suffix, "解析后缀失败");
        Assert.state(systemProperties.getFile().getAllowUpload().contains(suffix), "文件格式" + suffix + "不允许上传");

        String id = IdTool.uuidV7();

        // 生成文件的最终名称
        String objectName = genObjectName(id, suffix, null);

        // 文件管理信息
        SysFile sysFile = new SysFile();
        sysFile.setOriginName(originalFilename);
        sysFile.setSuffix(suffix);
        sysFile.setSize(size);
        sysFile.setObjectName(objectName);
        sysFile.setTradeNo(tradeNo);

        MediaType mediaType = MediaTypeFactory.getMediaType("." + suffix).orElse(null);
        if (mediaType != null) {
            sysFile.setMimeType(mediaType.toString());
        }
        sysFile.setType(MaterialType.parseBySuffix(suffix));


        File tempFile = FileUtil.createTempFile("." + suffix, true);
        FileUtils.copyInputStreamToFile(is, tempFile);


        // 保存原图，缩略图延迟到首次请求时生成
        fileOperator.saveFile(objectName, tempFile);
        FileUtil.del(tempFile);

        sysFile = sysFileRepository.save(sysFile);

        log.debug("上传文件结束 {}", objectName);

        return sysFile;
    }

    public SysFile getFileAndStream(String fileId, Integer w) throws Exception {
        Assert.hasText(fileId, "文件id不能为空");
        // 获取文件名
        SysFile sysFile = sysFileRepository.findById(fileId).orElse(null);
        Assert.notNull(sysFile, "文件数据记录不存在");

        // 返回文件字节码
        sysFile.setInputStream(getFileStream(sysFile, w));

        return sysFile;
    }

    public InputStream getFileStream(SysFile sysFile, Integer w) throws Exception {
        String objectName = getObjectName(sysFile, w);
        if (!fileOperator.exist(objectName)) {
            if (w != null && sysFile.getType() == MaterialType.IMAGE) {
                generateThumbnail(sysFile, w);
            } else {
                log.error("文件不存在 {}", objectName);
                throw new FileNotFoundException("文件不存在:" + objectName);
            }
        }

        if (!fileOperator.exist(objectName)) {
            // 缩略图生成失败，回退到原图
            log.warn("缩略图生成失败，返回原图 {}", objectName);
            return getFileStream(sysFile, null);
        }

        return fileOperator.getFileStream(objectName);
    }

    public void download(String id, HttpServletResponse response) throws Exception {
        // 获取文件信息结果集
        SysFile f = this.getFileAndStream(id, null);
        String fileName = f.getOriginName();
        DownloadTool.download(fileName, f.getInputStream(), f.getSize(), response);
    }

    /**
     * 下载到所属服务器
     *
     * @param id
     * @param localFile
     * @return
     * @throws Exception
     */
    public File downloadToLocal(String id, File localFile) throws Exception {
        SysFile sysFile = sysFileRepository.findById(id).orElse(null);
        fileOperator.downloadFile(sysFile.getObjectName(), localFile);
        return localFile;
    }

    public File downloadToLocalTemp(String id) throws Exception {
        SysFile sysFile = sysFileRepository.findById(id).orElse(null);
        File tempFile = FileUtil.createTempFile("." + sysFile.getSuffix(), true);
        fileOperator.downloadFile(sysFile.getObjectName(), tempFile);

        return tempFile;
    }

    public Optional<SysFile> findById(String id) {
        return sysFileRepository.findById(id);
    }

    public void fillAllImageUrl(SysFile sysFile) {
        List<Dict> urls = new ArrayList<>();
        String url = getPreviewUrl(sysFile.getId());
        if (sysFile.getType() == MaterialType.IMAGE) {
            for (int i = 0; i < IMAGE_SIZE.length; i++) {
                int size = IMAGE_SIZE[i];
                String sizeKey = IMAGE_SIZE_LABEL[i];
                Dict dict = Dict.of("size", size, "label", sizeKey, "url", url + "?w=" + size);
                urls.add(dict);
            }
        }
        // TODO
        // sysFile.putExtData("imageUrls", urls);
    }

    public Page<SysFile> findAll(Specification<SysFile> q, Pageable pageable) {
        Page<SysFile> page = sysFileRepository.findAll(q, pageable);
        for (SysFile sysFile : page) {
            this.fillAllImageUrl(sysFile);
        }
        return page;
    }

    public boolean isFileExist(String id) {
        if (StrUtil.isEmpty(id)) {
            return false;
        }
        SysFile file = sysFileRepository.findById(id).orElse(null);
        if (file == null) {
            return false;
        }

        return fileOperator.exist(file.getObjectName());
    }

    private void generateThumbnail(SysFile sysFile, int width) throws Exception {
        String originalName = getObjectName(sysFile, null);
        String thumbName = getObjectName(sysFile, width);

        File originalFile = FileUtil.createTempFile("." + sysFile.getSuffix(), true);
        FileUtil.del(originalFile);
        try {
            fileOperator.downloadFile(originalName, originalFile);
            File thumbFile = ImgTool.scale(originalFile, width);
            if (thumbFile != null) {
                try {
                    fileOperator.saveFile(thumbName, thumbFile);
                    log.info("缩略图已生成并缓存 {}", thumbName);
                } finally {
                    FileUtil.del(thumbFile);
                }
            }
        } finally {
            FileUtil.del(originalFile);
        }
    }

    private static boolean isBlockedMagicType(String magicType) {
        return Set.of("exe", "dll", "bat", "com", "msi", "scr", "pif", "reg", "vbs", "sh", "js")
                .contains(magicType);
    }

    private String genObjectName(String id, String suffix, Integer size) {
        String baseName = id;
        if (size != null) {
            baseName += "_" + size;
        }
        return DateUtil.format(new Date(), "yyyyMM") + "/" + baseName + "." + suffix;
    }

    private String getObjectName(SysFile file, Integer size) {
        if (size == null) {
            return file.getObjectName();
        }

        String end = "." + file.getSuffix();
        String sizeEnd = "_" + size + "." + file.getSuffix();
        String sizeObjectName = file.getObjectName().replace(end, sizeEnd);
        log.info("获取截取后的 {}", sizeObjectName);
        return sizeObjectName;
    }

}

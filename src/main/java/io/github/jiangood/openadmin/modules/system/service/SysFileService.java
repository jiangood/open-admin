package io.github.jiangood.openadmin.modules.system.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import io.github.jiangood.openadmin.util.DownloadTool;
import io.github.jiangood.openadmin.util.IdTool;
import io.github.jiangood.openadmin.util.RequestTool;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.enums.FileVisibility;
import io.github.jiangood.openadmin.framework.enums.MaterialType;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.system.SysFileConstants;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件服务类
 * <p>
 * 由于会被其他模块使用，不继承BaseService,减少干扰
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysFileService {




    private final SystemProperties systemProperties;
    private final FileOperator fileOperator;
    private final SysFileRepository sysFileRepository;
    private final SysUserService sysUserService;

    /**
     * 获得预览相对url（含 context-path），如 /example/file/xxx.jpg
     *
     * @param objectName
     * @return
     */
    public String getPreviewUrl(String objectName) {
        String contextPath = Optional.ofNullable(RequestTool.currentRequest())
                .map(HttpServletRequest::getContextPath)
                .orElse("");
        return contextPath + SysFileConstants.FILE_URL_PATTERN.replace("{objectName}", objectName);
    }

    @Transactional
    public void deleteByObjectName(String objectName) {
        SysFile sysFile = sysFileRepository.findByObjectName(objectName);
        if (sysFile == null) {
            return;
        }
        deleteFileInternal(sysFile);
    }

    /**
     * 删除单个文件：先标记为待删除，物理删除成功后再删 DB 记录；
     * 物理删除失败时保持 PENDING_DELETE 状态，由清理任务下轮重试
     *
     * @return 物理文件删除成功返回 true
     */
    @Transactional
    public boolean deleteFileInternal(SysFile file) {
        if (file.getStatus() != FileStatus.PENDING_DELETE) {
            file.setStatus(FileStatus.PENDING_DELETE);
            sysFileRepository.save(file);
        }
        if (!deletePhysicalFile(file)) {
            return false;
        }
        sysFileRepository.deleteById(file.getId());
        return true;
    }

    /**
     * 删除单个物理文件，失败仅记日志并返回 false（不抛出异常）
     */
    public boolean deletePhysicalFile(SysFile file) {
        try {
            fileOperator.delete(file.getObjectName());
            return true;
        } catch (Exception e) {
            log.error("删除物理文件失败: objectName={}, error={}", file.getObjectName(), e.getMessage());
            return false;
        }
    }

    public SysFile uploadFile(byte[] data, String originalFilename) throws Exception {
        return this.uploadFile(data, originalFilename, FileVisibility.PUBLIC);
    }

    public SysFile uploadFile(byte[] data, String originalFilename, FileVisibility visibility) throws Exception {
        return this.uploadFile(new ByteArrayInputStream(data), originalFilename, data.length, visibility);
    }

    /**
     * 上传网络文件
     *
     * @param origUrl
     * @return
     * @throws Exception
     */
    public SysFile uploadWebFile(String origUrl) throws Exception {
        return uploadWebFile(origUrl, FileVisibility.PUBLIC);
    }

    /**
     * 上传网络文件
     *
     * @param origUrl
     * @return
     * @throws Exception
     */
    public SysFile uploadWebFile(String origUrl, FileVisibility visibility) throws Exception {
        log.info("准备上传网络文件 {}", origUrl);
        File tempFile = new File(FileUtil.getTmpDir(), FileNameUtil.mainName(origUrl));


        long size = HttpUtil.downloadFile(origUrl, tempFile);
        log.info("下载文件完成 {}", FileUtil.readableFileSize(size));

        String suffix = FileNameUtil.getSuffix(origUrl);
        if (StrUtil.isEmpty(suffix)) {
            suffix = FileTypeUtil.getType(tempFile);
            tempFile = FileUtil.rename(tempFile, tempFile.getName() + "." + suffix, true);
        }


        SysFile sysFile = this.uploadFile(tempFile, visibility);
        FileUtil.del(tempFile);

        sysFile.setOrigUrl(origUrl);
        sysFileRepository.save(sysFile);

        return sysFile;
    }

    public SysFile uploadFile(File file) throws Exception {
        return uploadFile(file, FileVisibility.PUBLIC);
    }

    public SysFile uploadFile(File file, FileVisibility visibility) throws Exception {
        // 特殊处理后缀，如临时文件
        String suffix = FileNameUtil.getSuffix(file);
        if (StrUtil.isEmpty(suffix) || "tmp".equals(suffix)) {
            suffix = FileTypeUtil.getType(file, true);
        }

        String name = FileNameUtil.mainName(file) + "." + suffix;
        try (InputStream is = new FileInputStream(file)) {
            return this.uploadFile(is, name, file.length(), visibility);
        }
    }

    public SysFile uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, FileVisibility.PUBLIC);
    }

    public SysFile uploadFile(MultipartFile file, FileVisibility visibility) throws Exception {
        InputStream is = file.getInputStream();
        String name = file.getOriginalFilename();
        return this.uploadFile(is, name, file.getSize(), visibility);
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size) throws Exception {
        return uploadFile(is, originalFilename, size, FileVisibility.PUBLIC);
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size, FileVisibility visibility) throws Exception {
        if (visibility == null) {
            visibility = FileVisibility.PUBLIC;
        }
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

        if ("webp".equals(magicType)) {
            log.warn("上传文件真实类型为 webp");
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
        Set<String> allowSet = Set.of(systemProperties.getFile().getAllowUpload().split(","));
        Assert.state(allowSet.contains(suffix.toLowerCase()), "文件格式" + suffix + "不允许上传");

        String id = IdTool.uuidV7();

        // 生成文件的最终名称
        String objectName = genObjectName(id, suffix, visibility);

        // 文件管理信息
        SysFile sysFile = new SysFile();
        sysFile.setOriginName(originalFilename);
        sysFile.setSuffix(suffix);
        sysFile.setSize(size);
        sysFile.setObjectName(objectName);

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

    public SysFile getFileAndStream(String objectName) throws Exception {
        Assert.hasText(objectName, "文件objectName不能为空");
        // 获取文件记录
        SysFile sysFile = sysFileRepository.findByObjectName(objectName);
        Assert.notNull(sysFile, "文件数据记录不存在");

        // 返回文件字节码
        sysFile.setInputStream(getFileStream(sysFile));

        return sysFile;
    }

    public InputStream getFileStream(SysFile sysFile) throws Exception {
        if (!fileOperator.exist(sysFile.getObjectName())) {
            log.error("文件不存在 {}", sysFile.getObjectName());
            throw new FileNotFoundException("文件不存在:" + sysFile.getObjectName());
        }

        return fileOperator.getFileStream(sysFile.getObjectName());
    }

    public void download(String objectName, HttpServletResponse response) throws Exception {
        // 获取文件信息结果集
        SysFile f = this.getFileAndStream(objectName);
        String fileName = f.getOriginName();
        DownloadTool.download(fileName, f.getInputStream(), f.getSize(), response);
    }

    /**
     * 下载到所属服务器
     *
     * @param objectName
     * @param localFile
     * @return
     * @throws Exception
     */
    public File downloadToLocal(String objectName, File localFile) throws Exception {
        SysFile sysFile = sysFileRepository.findByObjectName(objectName);
        fileOperator.downloadFile(sysFile.getObjectName(), localFile);
        return localFile;
    }

    public File downloadToLocalTemp(String objectName) throws Exception {
        SysFile sysFile = sysFileRepository.findByObjectName(objectName);
        File tempFile = FileUtil.createTempFile("." + sysFile.getSuffix(), true);
        fileOperator.downloadFile(sysFile.getObjectName(), tempFile);

        return tempFile;
    }

    public Optional<SysFile> findById(String id) {
        return sysFileRepository.findById(id);
    }

    public SysFile findByObjectName(String objectName) {
        return sysFileRepository.findByObjectName(objectName);
    }

    private static final Pattern HTML_FILE_PATTERN = Pattern.compile(
            "file/((?:public|private)/\\d{6}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[a-zA-Z0-9]+)");

    @Transactional
    public void claimList(String joinTable, String joinId, List<String> oldObjectNames, List<String> newObjectNames) {
        if (newObjectNames != null && !newObjectNames.isEmpty()) {
            List<String> toConfirm = new ArrayList<>(newObjectNames);
            if (oldObjectNames != null) {
                toConfirm.removeAll(oldObjectNames);
            }
            if (!toConfirm.isEmpty()) {
                sysFileRepository.updateJoinRefByObjectNames(joinTable, joinId, toConfirm);
            }
        }
        if (oldObjectNames != null && !oldObjectNames.isEmpty() && newObjectNames != null) {
            List<String> toRemove = new ArrayList<>(oldObjectNames);
            toRemove.removeAll(newObjectNames);
            if (!toRemove.isEmpty()) {
                sysFileRepository.updateStatusByObjectNames(toRemove, FileStatus.PENDING_DELETE);
            }
        }
    }

    @Transactional
    public void claim(String joinTable, String joinId, String oldObjectName, String newObjectName) {
        claimList(joinTable, joinId, objectNameList(oldObjectName), objectNameList(newObjectName));
    }

    private List<String> objectNameList(String objectName) {
        return StrUtil.isBlank(objectName) ? List.of() : List.of(objectName);
    }

    @Transactional
    public void claimHtml(String joinTable, String joinId, String oldHtml, String newHtml) {
        claimList(joinTable, joinId, extractObjectNamesFromHtml(oldHtml), extractObjectNamesFromHtml(newHtml));
    }

    private List<String> extractObjectNamesFromHtml(String html) {
        if (StrUtil.isBlank(html)) {
            return List.of();
        }
        Matcher matcher = HTML_FILE_PATTERN.matcher(html);
        Set<String> objectNames = new LinkedHashSet<>();
        while (matcher.find()) {
            objectNames.add(matcher.group(1));
        }
        return new ArrayList<>(objectNames);
    }

    public Page<SysFile> findAll(Specification<SysFile> q, Pageable pageable) {
        Page<SysFile> page = sysFileRepository.findAll(q, pageable);
        for (SysFile file : page.getContent()) {
            file.setCreateUserLabel(sysUserService.getNameById(file.getCreateUser()));
        }
        return page;
    }

    public boolean isFileExist(String objectName) {
        if (StrUtil.isEmpty(objectName)) {
            return false;
        }
        SysFile file = sysFileRepository.findByObjectName(objectName);
        if (file == null) {
            return false;
        }

        return fileOperator.exist(file.getObjectName());
    }

    private static boolean isBlockedMagicType(String magicType) {
        return Set.of("exe", "dll", "bat", "com", "msi", "scr", "pif", "reg", "vbs", "sh", "js")
                .contains(magicType);
    }

    private String genObjectName(String id, String suffix, FileVisibility visibility) {
        return visibility.getPrefix() + "/" + DateUtil.format(new Date(), "yyyyMM") + "/" + id + "." + suffix;
    }

}

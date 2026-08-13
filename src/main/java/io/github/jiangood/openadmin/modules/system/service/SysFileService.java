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
     * 批量删除文件（按 id），逐条执行删除流程，返回物理删除成功的数量
     *
     * @param ids SysFile 主键 id 集合
     * @return 物理文件删除成功数量
     */
    @Transactional
    public int deleteBatch(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int success = 0;
        for (SysFile file : sysFileRepository.findAllById(ids)) {
            if (deleteFileInternal(file)) {
                success++;
            }
        }
        return success;
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
        try {
            sysFileRepository.deleteById(file.getId());
            return true;
        } catch (Exception e) {
            log.error("删除文件记录失败，保留待删除状态以便重试: id={}, error={}", file.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 删除单个物理文件（含缩略图），失败仅记日志并返回 false（不抛出异常）
     */
    public boolean deletePhysicalFile(SysFile file) {
        try {
            fileOperator.delete(file.getObjectName());
        } catch (Exception e) {
            log.error("删除物理文件失败: objectName={}, error={}", file.getObjectName(), e.getMessage());
            return false;
        }
        // 一并删除缩略图（不存在时忽略）
        try {
            fileOperator.delete(thumbKeyOf(file.getObjectName()));
        } catch (Exception e) {
            log.warn("删除缩略图失败: objectName={}, error={}", file.getObjectName(), e.getMessage());
        }
        return true;
    }

    /**
     * 由主文件 objectName 推导缩略图 objectName：
     * public/img/202401/{uuid}.jpg -> public/img/202401/{uuid}.thumb.jpg
     */
    public static String thumbKeyOf(String objectName) {
        int idx = objectName.lastIndexOf('.');
        if (idx < 0) {
            return objectName + SysFileConstants.THUMB_MARK;
        }
        return objectName.substring(0, idx) + SysFileConstants.THUMB_MARK + objectName.substring(idx);
    }

    public SysFile uploadFile(byte[] data, String originalFilename) throws Exception {
        return this.uploadFile(data, originalFilename, true);
    }

    public SysFile uploadFile(byte[] data, String originalFilename, boolean isPublic) throws Exception {
        return this.uploadFile(new ByteArrayInputStream(data), originalFilename, data.length, isPublic);
    }

    /**
     * 上传网络文件
     *
     * @param origUrl
     * @return
     * @throws Exception
     */
    public SysFile uploadWebFile(String origUrl) throws Exception {
        return uploadWebFile(origUrl, true);
    }

    /**
     * 上传网络文件
     *
     * @param origUrl
     * @return
     * @throws Exception
     */
    public SysFile uploadWebFile(String origUrl, boolean isPublic) throws Exception {
        log.info("准备上传网络文件 {}", origUrl);
        File tempFile = new File(FileUtil.getTmpDir(), FileNameUtil.mainName(origUrl));


        long size = HttpUtil.downloadFile(origUrl, tempFile);
        log.info("下载文件完成 {}", FileUtil.readableFileSize(size));

        String suffix = FileNameUtil.getSuffix(origUrl);
        if (StrUtil.isEmpty(suffix)) {
            suffix = FileTypeUtil.getType(tempFile);
            tempFile = FileUtil.rename(tempFile, tempFile.getName() + "." + suffix, true);
        }


        SysFile sysFile = this.uploadFile(tempFile, isPublic);
        FileUtil.del(tempFile);

        sysFile.setOrigUrl(origUrl);
        sysFileRepository.save(sysFile);

        return sysFile;
    }

    public SysFile uploadFile(File file) throws Exception {
        return uploadFile(file, true);
    }

    public SysFile uploadFile(File file, boolean isPublic) throws Exception {
        // 特殊处理后缀，如临时文件
        String suffix = FileNameUtil.getSuffix(file);
        if (StrUtil.isEmpty(suffix) || "tmp".equals(suffix)) {
            suffix = FileTypeUtil.getType(file, true);
        }

        String name = FileNameUtil.mainName(file) + "." + suffix;
        try (InputStream is = new FileInputStream(file)) {
            return this.uploadFile(is, name, file.length(), isPublic);
        }
    }

    public SysFile uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, true);
    }

    public SysFile uploadFile(MultipartFile file, boolean isPublic) throws Exception {
        InputStream is = file.getInputStream();
        String name = file.getOriginalFilename();
        return this.uploadFile(is, name, file.getSize(), isPublic);
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size) throws Exception {
        return uploadFile(is, originalFilename, size, true);
    }

    public SysFile uploadFile(InputStream is, String originalFilename, long size, boolean isPublic) throws Exception {
        log.info("上传文件:{} 大小:{}", originalFilename, FileUtil.readableFileSize(size));

        String suffix = validateAndGetSuffix(is, originalFilename);

        String id = IdTool.uuidV7();

        // 生成文件的最终名称
        String objectName = genObjectName(id, suffix, isPublic);

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

        File tempFile = FileUtil.createTempFile("." + suffix, true);
        FileUtils.copyInputStreamToFile(is, tempFile);


        // 保存原图，缩略图延迟到首次请求时生成
        fileOperator.saveFile(objectName, tempFile);
        FileUtil.del(tempFile);

        sysFile = sysFileRepository.save(sysFile);

        log.debug("上传文件结束 {}", objectName);

        return sysFile;
    }

    /**
     * 上传图片（压缩图 + 缩略图），一次请求同时存储两份文件
     * <p>
     * 压缩图 objectName: {prefix}/img/{yyyyMM}/{id}.{suffix}
     * 缩略图 objectName: {prefix}/img/{yyyyMM}/{id}.thumb.{suffix}
     * 缩略图不建独立 SysFile 记录，按命名约定随压缩图一起删除
     *
     * @return 压缩图对应的 SysFile 记录
     */
    public SysFile uploadImage(MultipartFile file, MultipartFile thumb, boolean isPublic) throws Exception {
        log.info("上传图片:{} 大小:{}, 缩略图:{} 大小:{}",
                file.getOriginalFilename(), FileUtil.readableFileSize(file.getSize()),
                thumb.getOriginalFilename(), FileUtil.readableFileSize(thumb.getSize()));

        String fileSuffix = validateAndGetSuffix(file.getInputStream(), file.getOriginalFilename());
        String thumbSuffix = validateAndGetSuffix(thumb.getInputStream(), thumb.getOriginalFilename());

        String id = IdTool.uuidV7();
        String objectName = genObjectName(id, fileSuffix, isPublic, true);
        String thumbObjectName = thumbKeyOf(objectName);

        // 保存压缩图
        File tempFile = FileUtil.createTempFile("." + fileSuffix, true);
        FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
        fileOperator.saveFile(objectName, tempFile);
        FileUtil.del(tempFile);

        // 保存缩略图（无独立 SysFile 记录）
        File tempThumb = FileUtil.createTempFile("." + thumbSuffix, true);
        FileUtils.copyInputStreamToFile(thumb.getInputStream(), tempThumb);
        fileOperator.saveFile(thumbObjectName, tempThumb);
        FileUtil.del(tempThumb);

        // 压缩图建立文件管理记录
        SysFile sysFile = new SysFile();
        sysFile.setOriginName(file.getOriginalFilename());
        sysFile.setSuffix(fileSuffix);
        sysFile.setSize(file.getSize());
        sysFile.setObjectName(objectName);

        MediaType mediaType = MediaTypeFactory.getMediaType("." + fileSuffix).orElse(null);
        if (mediaType != null) {
            sysFile.setMimeType(mediaType.toString());
        }
        sysFile.setType("image");

        sysFile = sysFileRepository.save(sysFile);

        log.debug("上传图片结束 {}", objectName);

        return sysFile;
    }

    /**
     * 校验文件真实类型（magic byte）并解析合法后缀，返回小写后缀
     */
    private String validateAndGetSuffix(InputStream is, String originalFilename) throws Exception {
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
        }

        Assert.hasText(suffix, "解析后缀失败");
        Set<String> allowSet = Set.of(systemProperties.getFile().getAllowUpload().split(","));
        Assert.state(allowSet.contains(suffix.toLowerCase()), "文件格式" + suffix + "不允许上传");
        return suffix.toLowerCase();
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

    /**
     * 按 objectName 直接获取文件流（不查 SysFile 记录，用于缩略图等衍生文件）
     */
    public InputStream getFileStreamByObjectName(String objectName) throws Exception {
        if (!fileOperator.exist(objectName)) {
            log.error("文件不存在 {}", objectName);
            throw new FileNotFoundException("文件不存在:" + objectName);
        }
        return fileOperator.getFileStream(objectName);
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

    /**
     * 提取富文本 HTML 中的框架文件 URL：
     * 支持 public/private 前缀、可选的 img/ 目录（uploadImage 产物）、query 串、带 context-path 的相对路径
     */
    private static final Pattern HTML_FILE_PATTERN = Pattern.compile(
            "file/((?:public|private)(?:/img)?/\\d{6}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[a-zA-Z0-9]+)(?:\\?[\\w=&.-]*)?");


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

    /**
     * 判断物理文件是否真实存在（不查 SysFile 记录，用于缩略图等衍生文件）
     */
    public boolean isPhysicalFileExist(String objectName) {
        return StrUtil.isNotEmpty(objectName) && fileOperator.exist(objectName);
    }

    private static boolean isBlockedMagicType(String magicType) {
        return Set.of("exe", "dll", "bat", "com", "msi", "scr", "pif", "reg", "vbs", "sh", "js")
                .contains(magicType);
    }

    private String genObjectName(String id, String suffix, boolean isPublic) {
        return genObjectName(id, suffix, isPublic, false);
    }

    /**
     * 生成 objectName；image=true 时图片单独存放 img 目录：
     * public/202401/{id}.jpg  ->  public/img/202401/{id}.jpg
     */
    private String genObjectName(String id, String suffix, boolean isPublic, boolean image) {
        String prefix = isPublic ? SysFileConstants.PUBLIC_PREFIX : SysFileConstants.PRIVATE_PREFIX;
        String dir = prefix + "/" + DateUtil.format(new Date(), "yyyyMM");
        if (image) {
            dir = prefix + "/" + SysFileConstants.IMAGE_DIR + "/" + DateUtil.format(new Date(), "yyyyMM");
        }
        return dir + "/" + id + "." + suffix;
    }

}

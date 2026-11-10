package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysBrowserFingerprint;
import io.github.jiangood.openadmin.modules.system.repository.SysBrowserFingerprintRepository;
import io.github.jiangood.openadmin.util.BrowserFingerprintTool;
import io.github.jiangood.openadmin.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 浏览器指纹备注。
 */
@Service
@RequiredArgsConstructor
public class SysBrowserFingerprintService extends BaseService<SysBrowserFingerprint> {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int REMARK_MAX_LENGTH = 500;

    private final SysBrowserFingerprintRepository fingerprintRepository;

    /**
     * 按指纹批量查备注，供日志分页回填，避免 N+1 查询。
     */
    public Map<String, SysBrowserFingerprint> findMap(Collection<String> fingerprints) {
        if (fingerprints == null || fingerprints.isEmpty()) {
            return Map.of();
        }
        Spec<SysBrowserFingerprint> q = Spec.of();
        q.in(SysBrowserFingerprint.Fields.fingerprint, fingerprints);

        Map<String, SysBrowserFingerprint> result = new HashMap<>();
        for (SysBrowserFingerprint item : findAll(q, Sort.unsorted())) {
            result.put(item.getFingerprint(), item);
        }
        return result;
    }

    /**
     * 按备注关键字查找指纹：支持备注名、补充说明的模糊匹配。
     */
    public List<String> findFingerprintsByRemark(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String value = keyword.trim();
        Spec<SysBrowserFingerprint> q = Spec.of();
        q.or(sub -> {
            sub.like(SysBrowserFingerprint.Fields.name, value);
            sub.like(SysBrowserFingerprint.Fields.remark, value);
        });
        return findAll(q, Sort.unsorted()).stream().map(SysBrowserFingerprint::getFingerprint).toList();
    }

    /**
     * 保存指纹备注。备注名与说明都为空时删除该指纹的备注。
     *
     * @return 保存后的备注；清除备注时返回 null
     */
    @Transactional
    public SysBrowserFingerprint saveRemark(String fingerprint, String name, String remark) {
        String normalizedFingerprint = BrowserFingerprintTool.normalize(fingerprint);
        if (normalizedFingerprint == null) {
            throw new BusinessException("浏览器指纹不能为空");
        }
        String normalizedName = truncate(name, NAME_MAX_LENGTH);
        String normalizedRemark = truncate(remark, REMARK_MAX_LENGTH);

        SysBrowserFingerprint existing = findByField(
                SysBrowserFingerprint.Fields.fingerprint, normalizedFingerprint);
        if (normalizedName == null && normalizedRemark == null) {
            if (existing != null) {
                fingerprintRepository.delete(existing);
            }
            return null;
        }
        if (existing == null) {
            existing = new SysBrowserFingerprint();
            existing.setFingerprint(normalizedFingerprint);
        }
        existing.setName(normalizedName);
        existing.setRemark(normalizedRemark);
        return fingerprintRepository.save(existing);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}

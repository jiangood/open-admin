package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import io.github.jiangood.openadmin.modules.system.repository.SysManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SysManualService extends BaseService<SysManual> {

    private final SysManualRepository sysManualRepository;

    @Transactional
    public SysManual save(SysManual input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            int maxVersion = sysManualRepository.findMaxVersion(input.getName());
            input.setVersion(maxVersion + 1);
            return repository.save(input);
        }

        repository.updateField(input, requestKeys);
        return repository.findById(input.getId()).orElse(null);
    }
}

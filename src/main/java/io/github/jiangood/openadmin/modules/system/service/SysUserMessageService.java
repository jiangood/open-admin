package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.entity.SysUserMessage;
import io.github.jiangood.openadmin.modules.system.repository.SysUserMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class SysUserMessageService extends BaseService<SysUserMessage> {

    private final SysUserMessageRepository sysUserMessageRepository;

    public SysUserMessageService(SysUserMessageRepository sysUserMessageRepository) {
        super(sysUserMessageRepository);
        this.sysUserMessageRepository = sysUserMessageRepository;
    }

    public Page<SysUserMessage> findByUser(String id, Boolean read, Pageable pageable) {
        Spec<SysUserMessage> spec = Spec.<SysUserMessage>of().eq(SysUserMessage.Fields.user + ".id", id);
        if (read != null) {
            spec.eq(SysUserMessage.Fields.read, read);
        }

        return sysUserMessageRepository.findAll(spec, pageable);
    }

    public long countUnReadByUser(String id) {
        return sysUserMessageRepository.count(Spec.<SysUserMessage>of().eq(SysUserMessage.Fields.user + ".id", id).eq(SysUserMessage.Fields.read, false));
    }

    @Transactional
    public void save(String userId, String title, String content) {
        SysUserMessage msg = new SysUserMessage();
        msg.setUser(new SysUser(userId));
        msg.setTitle(title);
        msg.setContent(content);
        sysUserMessageRepository.save(msg);
    }

    public void read(String id) {
        SysUserMessage db = sysUserMessageRepository.findOne(id);
        db.setReadTime(new Date());
        db.setRead(true);
        sysUserMessageRepository.save(db);
    }

    @Transactional
    public SysUserMessage save(SysUserMessage input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return sysUserMessageRepository.save(input);
        }

        sysUserMessageRepository.updateField(input, requestKeys);
        return sysUserMessageRepository.findOne(input.getId());
    }

    @Transactional
    public void delete(String id) {
        sysUserMessageRepository.deleteById(id);
    }

    public SysUserMessage detail(String id) {
        return sysUserMessageRepository.findOne(id);
    }

    public SysUserMessage get(String id) {
        return sysUserMessageRepository.findOne(id);
    }

}

package io.admin.modules.system.service;

import io.admin.framework.data.service.BaseService;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.dao.SysUserMessageDao;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.entity.SysUserMessage;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class SysUserMessageService extends BaseService<SysUserMessage> {

    @Resource
    SysUserMessageDao sysUserMsgDao;

    public Page<SysUserMessage> findByUser(String id, Boolean read, Pageable pageable) {
        Spec<SysUserMessage> spec = Spec.<SysUserMessage>of().eq(SysUserMessage.Fields.user + ".id", id);
        if(read != null){
            spec.eq(SysUserMessage.Fields.isRead , read);
        }

        return sysUserMsgDao.findAll(spec, pageable);
    }

    public long countUnReadByUser(String id) {
       return sysUserMsgDao.count(Spec.<SysUserMessage>of().eq(SysUserMessage.Fields.user + ".id", id).eq(SysUserMessage.Fields.isRead , false));
    }

    @Transactional
    public void save(String userId, String title, String content){
        SysUserMessage msg = new SysUserMessage();
        msg.setUser(new SysUser(userId));
        msg.setTitle(title);
        msg.setContent(content);
        sysUserMsgDao.save(msg);
    }

    public void read(String id) {
        SysUserMessage db = sysUserMsgDao.findOne(id);
        db.setReadTime(new Date());
        db.setIsRead(true);
        sysUserMsgDao.save(db);
    }
}

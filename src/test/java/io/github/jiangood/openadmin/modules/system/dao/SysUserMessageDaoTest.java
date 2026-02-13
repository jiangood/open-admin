package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.entity.SysUserMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class SysUserMessageDaoTest {

    @Autowired
    private SysUserMessageDao sysUserMessageDao;

    @Autowired
    private SysUserDao sysUserDao;

    @Test
    void testSave() {
        SysUser user = new SysUser();
        user.setAccount("testuser");
        user.setName("Test User");
        user.setEnabled(true);
        sysUserDao.save(user);

        SysUserMessage message = new SysUserMessage();
        message.setTitle("Test Message");
        message.setContent("This is a test message");
        message.setUser(user);
        message.setIsRead(false);
        message.setReadTime(null);

        SysUserMessage savedMessage = sysUserMessageDao.save(message);
        assertNotNull(savedMessage.getId());
        assertEquals("Test Message", savedMessage.getTitle());
        assertEquals("This is a test message", savedMessage.getContent());
        assertEquals(user.getId(), savedMessage.getUser().getId());
        assertFalse(savedMessage.getIsRead());
        assertNull(savedMessage.getReadTime());
    }

    @Test
    void testFindById() {
        SysUser user = new SysUser();
        user.setAccount("testuser");
        user.setName("Test User");
        user.setEnabled(true);
        sysUserDao.save(user);

        SysUserMessage message = new SysUserMessage();
        message.setTitle("Test Message");
        message.setContent("This is a test message");
        message.setUser(user);
        message.setIsRead(false);
        message.setReadTime(null);

        SysUserMessage savedMessage = sysUserMessageDao.save(message);
        SysUserMessage foundMessage = sysUserMessageDao.findById(savedMessage.getId()).orElse(null);
        assertNotNull(foundMessage);
        assertEquals(savedMessage.getId(), foundMessage.getId());
    }

    @Test
    void testDeleteById() {
        SysUser user = new SysUser();
        user.setAccount("testuser");
        user.setName("Test User");
        user.setEnabled(true);
        sysUserDao.save(user);

        SysUserMessage message = new SysUserMessage();
        message.setTitle("Test Message");
        message.setContent("This is a test message");
        message.setUser(user);
        message.setIsRead(false);
        message.setReadTime(null);

        SysUserMessage savedMessage = sysUserMessageDao.save(message);
        sysUserMessageDao.deleteById(savedMessage.getId());
        SysUserMessage foundMessage = sysUserMessageDao.findById(savedMessage.getId()).orElse(null);
        assertNull(foundMessage);
    }

    @Test
    void testFindAll() {
        SysUser user = new SysUser();
        user.setAccount("testuser");
        user.setName("Test User");
        user.setEnabled(true);
        sysUserDao.save(user);

        for (int i = 0; i < 3; i++) {
            SysUserMessage message = new SysUserMessage();
            message.setTitle("Test Message " + i);
            message.setContent("This is test message " + i);
            message.setUser(user);
            message.setIsRead(false);
            message.setReadTime(null);
            sysUserMessageDao.save(message);
        }

        List<SysUserMessage> messages = sysUserMessageDao.findAll();
        assertTrue(messages.size() >= 3);
    }

    @Test
    void testUpdate() {
        SysUser user = new SysUser();
        user.setAccount("testuser");
        user.setName("Test User");
        user.setEnabled(true);
        sysUserDao.save(user);

        SysUserMessage message = new SysUserMessage();
        message.setTitle("Test Message");
        message.setContent("This is a test message");
        message.setUser(user);
        message.setIsRead(false);
        message.setReadTime(null);

        SysUserMessage savedMessage = sysUserMessageDao.save(message);
        savedMessage.setTitle("Updated Test Message");
        savedMessage.setIsRead(true);
        savedMessage.setReadTime(new Date());

        SysUserMessage updatedMessage = sysUserMessageDao.save(savedMessage);
        assertEquals("Updated Test Message", updatedMessage.getTitle());
        assertTrue(updatedMessage.getIsRead());
        assertNotNull(updatedMessage.getReadTime());
    }
}

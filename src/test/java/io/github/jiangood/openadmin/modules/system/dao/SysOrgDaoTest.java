package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.enums.OrgType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import io.github.jiangood.openadmin.OpenAdminConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SysOrgDaoTest {

    @Autowired
    private SysOrgDao sysOrgDao;

    private SysOrg testOrg;

    @BeforeEach
    public void setUp() {
        // 清理测试数据
        sysOrgDao.deleteAll();

        // 创建测试机构
        testOrg = new SysOrg();
        testOrg.setName("测试机构");
        testOrg.setType(OrgType.TYPE_UNIT);
        testOrg.setEnabled(true);
        testOrg.setSeq(1);
        testOrg = sysOrgDao.save(testOrg);
    }

    @Test
    public void testSave() {
        SysOrg org = new SysOrg();
        org.setName("新测试机构");
        org.setType(OrgType.TYPE_UNIT);
        org.setEnabled(true);
        org.setSeq(2);

        SysOrg savedOrg = sysOrgDao.save(org);
        assertNotNull(savedOrg.getId());
        assertEquals("新测试机构", savedOrg.getName());
    }


}

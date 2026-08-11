package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.modules.system.dto.request.OrgReq;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import io.github.jiangood.openadmin.modules.system.repository.SysOrgRepository;
import io.github.jiangood.openadmin.util.BeanTool;
import io.github.jiangood.openadmin.util.JsonTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 机构 update 走 controller 相同的「JSON → OrgReq → BeanTool.copy → service.save」链路，
 * 防止更新时因 id 丢失被当成新增插入。
 */
@SpringBootTest
@Transactional
public class SysOrgServiceTest {

    @Autowired
    private SysOrgRepository sysOrgRepository;

    @Autowired
    private SysOrgService sysOrgService;

    @Test
    void update_shouldUpdateInPlaceInsteadOfCreatingNewOrg() throws Exception {
        SysOrg org = new SysOrg();
        org.setName("默认单位");
        org.setEnabled(true);
        org.setType(1);
        sysOrgRepository.save(org);
        long countBefore = sysOrgRepository.count();

        String body = "{\"id\":\"" + org.getId()
                + "\",\"name\":\"默认单位-改名\",\"enabled\":true,\"type\":1}";
        OrgReq req = JsonTool.jsonToBean(body, OrgReq.class);
        SysOrg input = BeanTool.copy(req, new SysOrg());
        input.setType(req.getType());

        sysOrgService.save(input, List.of("id", "name", "enabled", "type"));

        assertEquals(countBefore, sysOrgRepository.count(), "更新不应该新增机构");
        SysOrg db = sysOrgRepository.findById(org.getId()).orElse(null);
        assertEquals("默认单位-改名", db.getName());
        assertEquals(org.getId(), db.getId());
    }
}

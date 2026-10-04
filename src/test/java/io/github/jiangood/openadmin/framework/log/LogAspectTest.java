package io.github.jiangood.openadmin.framework.log;

import io.github.jiangood.openadmin.modules.system.dto.request.UserReq;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogAspectTest {

    @Test
    void toJson_shouldMaskSensitiveFields() throws Exception {
        UserReq req = new UserReq();
        req.setId("1");
        req.setAccount("admin");
        req.setName("测试");
        req.setPassword("SuperSecret123");

        Method toJson = LogAspect.class.getDeclaredMethod("toJson", Object.class);
        toJson.setAccessible(true);
        String json = (String) toJson.invoke(null, req);

        assertNotNull(json);
        assertFalse(json.contains("SuperSecret123"), "密码不应明文出现在操作日志 JSON 中: " + json);
        assertTrue(json.contains("admin"), "非敏感字段应正常记录");
    }

    @Test
    void toJson_shouldKeepOtherRequestBodiesIntact() throws Exception {
        UserReq req = new UserReq();
        req.setId("2");
        req.setName("正常字段");

        Method toJson = LogAspect.class.getDeclaredMethod("toJson", Object.class);
        toJson.setAccessible(true);
        String json = (String) toJson.invoke(null, req);

        assertNotNull(json);
        assertTrue(json.contains("正常字段"));
        assertTrue(json.contains("2"));
    }
}
package io.github.jiangood.openadmin.framework.log;

import io.github.jiangood.openadmin.modules.system.dto.request.UserReq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogAspectTest {

    @AfterEach
    void tearDown() {
        OperationLogContext.setDetail(null);
    }

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

    @Test
    void renderDetailTemplate_shouldResolveParameterNames() throws Exception {
        Method method = Sample.class.getDeclaredMethod("create", UserReq.class);
        UserReq req = new UserReq();
        req.setName("张三");
        req.setAccount("zhangsan");

        String detail = LogAspect.renderDetailTemplate(
                "新增用户「${#input.name}（${#input.account}）」", method, new Object[]{req});

        assertEquals("新增用户「张三（zhangsan）」", detail);
    }

    @Test
    void renderDetailTemplate_shouldSupportIndexVariableAndNullValue() throws Exception {
        Method method = Sample.class.getDeclaredMethod("create", UserReq.class);
        UserReq req = new UserReq();
        req.setName("李四");

        String detail = LogAspect.renderDetailTemplate("修改用户「${#p0.name}」", method, new Object[]{req});
        assertEquals("修改用户「李四」", detail);

        String missing = LogAspect.renderDetailTemplate("修改用户「${#p0.phone}」", method, new Object[]{req});
        assertEquals("修改用户「」", missing);
    }

    @Test
    void renderDetailTemplate_shouldKeepPlainTextAndTolerateBadExpression() throws Exception {
        Method method = Sample.class.getDeclaredMethod("create", UserReq.class);

        String plain = LogAspect.renderDetailTemplate("发送测试邮件", method, new Object[]{new UserReq()});
        assertEquals("发送测试邮件", plain);

        String bad = LogAspect.renderDetailTemplate("用户「${#input.notExist}」", method, new Object[]{new UserReq()});
        assertEquals("用户「」", bad);

        String nullArg = LogAspect.renderDetailTemplate("用户「${#input.name}」", method, new Object[]{null});
        assertEquals("用户「」", nullArg);
    }

    @Test
    void renderDetailTemplate_shouldSupportElvisFallback() throws Exception {
        Method method = Sample.class.getDeclaredMethod("create", UserReq.class);
        UserReq req = new UserReq();
        req.setAccount("zhangsan");

        String detail = LogAspect.renderDetailTemplate(
                "修改用户「${#input.name ?: #input.account ?: #input.id}」", method, new Object[]{req});

        assertEquals("修改用户「zhangsan」", detail);
    }

    @Test
    void operationLogContext_shouldRestoreOuterDetail() {
        OperationLogContext.setDetail("outer");
        String previous = OperationLogContext.enter();

        assertNull(OperationLogContext.getDetail(), "进入内层 @Log 方法后应清空外层详情");
        OperationLogContext.setDetail("inner");
        assertEquals("inner", OperationLogContext.getDetail());

        OperationLogContext.restore(previous);
        assertEquals("outer", OperationLogContext.getDetail());
    }

    @SuppressWarnings("unused")
    private static class Sample {
        void create(UserReq input) {
        }
    }
}

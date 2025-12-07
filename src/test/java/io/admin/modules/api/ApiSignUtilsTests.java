package io.admin.modules.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApiSignUtilsTests {

    @Test
    void testSign() {
        String appId = "testAppId";
        String appSecret = "testSecret";
        long timestamp = 1234567890L;
        
        String sign = ApiSignUtils.sign(appId, appSecret, timestamp);
        
        // 验证签名生成成功（不为null或空）
        assertNotNull(sign);
        assertFalse(sign.isEmpty());
        
        // 验证相同参数生成相同签名
        String sign2 = ApiSignUtils.sign(appId, appSecret, timestamp);
        assertEquals(sign, sign2);
        
        // 验证不同参数生成不同签名
        String sign3 = ApiSignUtils.sign("differentAppId", appSecret, timestamp);
        assertNotEquals(sign, sign3);
    }
}
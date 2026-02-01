package io.github.jiangood.openadmin.modules.api.defaults;

import io.github.jiangood.openadmin.modules.api.ApiMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class MathApi {


    @ApiMapping(action = "math.register", name = "加法计算", desc = "根据输入参数a，b，计算a+b的结果")
    public int add(int a, int b) {
        return a + b;
    }


    // 注意区别请求和响应格式的区别
    @ApiMapping(action = "math.sub", name = "减法", desc = "根据输入参数a，b，计算a-b的结果")
    public MathOutput sub(MathRequest addInput) {
        int a = addInput.getA();
        int b = addInput.getB();
        int sum = a - b;

        return new MathOutput(sum);
    }

    @Data
    public static class MathRequest {
        private int a;
        private int b;
    }

    @AllArgsConstructor
    @Data
    public static class MathOutput {
        private int result;
    }

}

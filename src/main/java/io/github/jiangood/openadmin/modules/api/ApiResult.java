package io.github.jiangood.openadmin.modules.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;

/***
 * 0	        成功	         0
 * 1000-1999	系统通用错误	1001: 系统错误
 * 2000-2999	参数校验错误	2001: 参数缺失
 * 3000-3999	认证授权错误	3001: 未登录
 * 4000-4999	资源错误	    4001: 资源不存在
 * 5000-5999	业务逻辑错误	5001: 订单状态异常
 * 6000-6999	第三方服务错误	6001: 上游超时
 * @param <T>
 */
@Data
@Schema(description = "通用返回结果")
public class ApiResult<T> {

    @Schema(description = "状态码", example = "0")
    private int code;

    private String message;

    @Schema(description = "结果数据")
    private T data;


    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(0);
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}

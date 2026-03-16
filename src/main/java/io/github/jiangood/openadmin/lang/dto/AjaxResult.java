package io.github.jiangood.openadmin.lang.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.jiangood.openadmin.framework.MessageConst;
import io.github.jiangood.openadmin.lang.JsonTool;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应结果数据
 * <p>
 * 参考大多数语言，如c语言， 0表示返回成功
 */
@Getter
@Setter
public class AjaxResult {

    public static final int SUCCESS = 200;
    public static final int FAILURE = 500;


    public static final AjaxResult FORBIDDEN = AjaxResult.err(HttpStatus.FORBIDDEN.value(), MessageConst.MGS_FORBIDDEN);


    public static final AjaxResult UNAUTHORIZED = AjaxResult.err(HttpStatus.UNAUTHORIZED.value(), MessageConst.MSG_UNAUTHORIZED);


    boolean success;

    Integer code;

    Object data;

    String message;
    /**
     * 动态字段，处理实体中不包含的字段
     * 例如状态字段 status, 转成json希望动态增加字段 statusLabel
     */
    @Setter(AccessLevel.NONE) // lombok不生成setter
    @JsonAnySetter
    private Map<String, Object> extData = new HashMap<>();

    public AjaxResult() {
        this(true);
    }


    public AjaxResult(boolean success) {
        this.success = success;
        this.code = success ? SUCCESS : FAILURE;
    }

    public static AjaxResult ok() {
        return new AjaxResult(true);
    }

    public static AjaxResult ok(String msg, Object data) {
        return ok().msg(msg).data(data);
    }

    public static AjaxResult ok(String msg) {
        return ok().msg(msg);
    }

    public static AjaxResult err() {
        return new AjaxResult(false);
    }

    public static AjaxResult err(String msg) {
        return err().msg(msg);
    }

    public static AjaxResult err(int code, String msg) {
        return err().msg(msg).code(code);
    }

    public AjaxResult code(int code) {
        this.code = code;
        return this;
    }

    public AjaxResult data(Object data) {
        this.data = data;
        return this;
    }

    public AjaxResult msg(String msg) {
        this.message = msg;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtData() {
        return extData;
    }

    /**
     * 加入额外字段
     *
     * @param key
     * @param value
     * @return
     */
    public AjaxResult putExtData(String key, Object value) {
        extData.put(key, value);
        return this;
    }


    @SuppressWarnings("rawtypes")
    public AjaxResult data(String key, Object value) {
        if (data == null) {
            this.data = new HashMap<>();
        } else {
            if (!(data instanceof Map)) {
                data = JsonTool.jsonToMapQuietly(JsonTool.toJsonQuietly(data));
            }
        }
        Map<String, Object> map = (Map<String, Object>) data;
        map.put(key, value);
        return this;
    }


}

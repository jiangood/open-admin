package io.github.jiangood.sa.framework.enums;

/**
 * 枚举代码接口
 * 所有需要支持通用转换的枚举都应实现此接口
 * 实现该接口的枚举会自动保持到数据字典包，方便前端使用
 * <p>
 * 例如
 *
 * @AllArgsConstructor
 * @Getter public enum OrgType implements CodeEnum {
 * TYPE_UNIT(10, "单位"),
 * TYPE_DEPT(20, "部门");
 * <p>
 * private final int code;
 * private final String msg;
 * }
 * @gendoc
 */
public interface CodeEnum {
    int getCode();

    String getMsg();
}

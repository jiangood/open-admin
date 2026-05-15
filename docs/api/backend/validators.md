# 后端验证器

## 验证注解列表

| 注解 | 用途 |
|------|------|
| `@ValidateMobile` | 手机号码格式 |
| `@ValidateIdCard` | 身份证号码格式 |
| `@ValidateEmail` | 邮箱格式 |
| `@ValidateCreditCode` | 统一社会信用代码 |
| `@ValidateCarDrivingLicence` | 驾驶证格式 |
| `@ValidateChineseName` | 中文姓名 |
| `@ValidateContainsChinese` | 包含中文字符 |
| `@ValidateDate` | 日期格式 |
| `@ValidateGeneral` | 通用正则验证 |
| `@ValidateHex` | 十六进制格式 |
| `@ValidateIp` | IP 地址 |
| `@ValidateIpv4` | IPv4 地址 |
| `@ValidatePassword` | 密码格式 |
| `@ValidatePlateNumber` | 车牌号 |
| `@ValidateStartWithLetter` | 以字母开头 |
| `@ValidateYearMonth` | 年月格式 |
| `@ValidateYearQuarter` | 年季度格式 |
| `@ValidateZipCode` | 邮政编码 |

## 使用示例

```java
public class User {
    @ValidateMobile(message = "请输入正确的手机号码")
    private String phone;

    @ValidateIdCard(message = "请输入正确的身份证号码")
    private String idCard;

    @ValidatePassword(message = "密码必须包含字母和数字，长度至少8位")
    private String password;
}
```

## Controller 中使用

```java
@PostMapping("create")
public AjaxResult create(@Validated @RequestBody User user) {
    return AjaxResult.ok().msg("创建成功");
}

@PostMapping("update")
public AjaxResult update(@Validated @RequestBody User user) {
    return AjaxResult.ok().msg("更新成功");
}
```

## 分组验证

```java
public class User {
    public interface AddGroup {}
    public interface UpdateGroup {}

    @NotBlank(groups = UpdateGroup.class, message = "ID不能为空")
    private String id;

    @ValidateMobile(groups = {AddGroup.class, UpdateGroup.class})
    private String phone;
}

// 使用
@PostMapping("create")
public AjaxResult create(@Validated(User.AddGroup.class) @RequestBody User user) { ... }

@PostMapping("update")
public AjaxResult update(@Validated(User.UpdateGroup.class) @RequestBody User user) { ... }
```

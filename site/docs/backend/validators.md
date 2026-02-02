# 后端验证器

本文档介绍了 open-admin 后端框架中提供的数据验证注解。

## 验证器列表

### ValidateMobile

验证手机号码格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateMobile;

public class User {
    @ValidateMobile(message = "请输入正确的手机号码")
    private String phone;
    
    // 其他字段...
}
```

### ValidateIdCard

验证身份证号码格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateIdCard;

public class User {
    @ValidateIdCard(message = "请输入正确的身份证号码")
    private String idCard;
    
    // 其他字段...
}
```

### ValidateEmail

验证邮箱格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateEmail;

public class User {
    @ValidateEmail(message = "请输入正确的邮箱地址")
    private String email;
    
    // 其他字段...
}
```

### ValidateCreditCode

验证统一社会信用代码格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateCreditCode;

public class Company {
    @ValidateCreditCode(message = "请输入正确的统一社会信用代码")
    private String creditCode;
    
    // 其他字段...
}
```

### ValidateCarDrivingLicence

验证驾驶证格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateCarDrivingLicence;

public class Driver {
    @ValidateCarDrivingLicence(message = "请输入正确的驾驶证号码")
    private String drivingLicence;
    
    // 其他字段...
}
```

### ValidateChineseName

验证中文姓名格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateChineseName;

public class User {
    @ValidateChineseName(message = "请输入正确的中文姓名")
    private String name;
    
    // 其他字段...
}
```

### ValidateContainsChinese

验证包含中文字符。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateContainsChinese;

public class Product {
    @ValidateContainsChinese(message = "产品名称必须包含中文字符")
    private String name;
    
    // 其他字段...
}
```

### ValidateDate

验证日期格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateDate;

public class Order {
    @ValidateDate(message = "请输入正确的日期格式")
    private String orderDate;
    
    // 其他字段...
}
```

### ValidateGeneral

通用验证。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateGeneral;

public class User {
    @ValidateGeneral(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    // 其他字段...
}
```

### ValidateHex

验证十六进制格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateHex;

public class Color {
    @ValidateHex(message = "请输入正确的十六进制颜色值")
    private String colorCode;
    
    // 其他字段...
}
```

### ValidateIp

验证IP地址格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateIp;

public class Server {
    @ValidateIp(message = "请输入正确的IP地址")
    private String ipAddress;
    
    // 其他字段...
}
```

### ValidateIpv4

验证IPv4地址格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateIpv4;

public class Server {
    @ValidateIpv4(message = "请输入正确的IPv4地址")
    private String ipv4Address;
    
    // 其他字段...
}
```

### ValidatePassword

验证密码格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidatePassword;

public class User {
    @ValidatePassword(message = "密码必须包含字母和数字，长度至少8位")
    private String password;
    
    // 其他字段...
}
```

### ValidatePlateNumber

验证车牌号格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidatePlateNumber;

public class Car {
    @ValidatePlateNumber(message = "请输入正确的车牌号")
    private String plateNumber;
    
    // 其他字段...
}
```

### ValidateStartWithLetter

验证以字母开头。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateStartWithLetter;

public class Product {
    @ValidateStartWithLetter(message = "产品编码必须以字母开头")
    private String code;
    
    // 其他字段...
}
```

### ValidateYearMonth

验证年月格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateYearMonth;

public class Report {
    @ValidateYearMonth(message = "请输入正确的年月格式，如：2024-01")
    private String yearMonth;
    
    // 其他字段...
}
```

### ValidateYearQuarter

验证年季度格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateYearQuarter;

public class Report {
    @ValidateYearQuarter(message = "请输入正确的年季度格式，如：2024-Q1")
    private String yearQuarter;
    
    // 其他字段...
}
```

### ValidateZipCode

验证邮政编码格式。

**使用示例**：

```java
import io.github.jiangood.openadmin.validator.ValidateZipCode;

public class Address {
    @ValidateZipCode(message = "请输入正确的邮政编码")
    private String zipCode;
    
    // 其他字段...
}
```

## 验证器使用说明

### 在Controller中使用

```java
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.system.entity.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/user")
public class UserController {

    @PostMapping("save")
    public AjaxResult save(@Validated @RequestBody User user) {
        // 业务逻辑
        return AjaxResult.ok().msg("保存成功");
    }
}
```

### 分组验证

```java
import io.github.jiangood.openadmin.validator.ValidateMobile;
import jakarta.validation.constraints.NotBlank;

public class User {
    
    public interface AddGroup {}
    public interface UpdateGroup {}
    
    @NotBlank(groups = UpdateGroup.class, message = "ID不能为空")
    private String id;
    
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "姓名不能为空")
    private String name;
    
    @ValidateMobile(groups = {AddGroup.class, UpdateGroup.class}, message = "请输入正确的手机号码")
    private String phone;
    
    // 其他字段...
}

// 在Controller中使用
@PostMapping("add")
public AjaxResult add(@Validated(User.AddGroup.class) @RequestBody User user) {
    // 业务逻辑
    return AjaxResult.ok().msg("添加成功");
}

@PostMapping("update")
public AjaxResult update(@Validated(User.UpdateGroup.class) @RequestBody User user) {
    // 业务逻辑
    return AjaxResult.ok().msg("更新成功");
}
```
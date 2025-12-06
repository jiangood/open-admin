package io.admin.framework.data.specification;

import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 假设的 User 实体
@Entity
@Table(name = "t_user")
@Data // 包含 @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor // 无参构造函数
@Builder // 启用 Builder 模式 (用于更灵活的测试数据创建)
public class User extends BaseEntity {

    private String username;
    private Integer age;



    // 针对测试，创建一个方便的自定义构造函数（不包含ID）
    public User(String username, Integer age) {
        this.username = username;
        this.age = age;
    }
}




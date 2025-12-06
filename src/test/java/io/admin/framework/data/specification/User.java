package io.admin.framework.data.specification;
import io.admin.framework.data.specification.Department;
import io.admin.framework.data.specification.Role;
import lombok.*;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 假设的 User 实体
@Entity
@Table(name = "t_user")
@Data // 包含 @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor // 无参构造函数
@AllArgsConstructor // 全参构造函数（测试数据初始化需要）
@Builder // 启用 Builder 模式 (用于更灵活的测试数据创建)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Integer age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department dept; // 关联字段

    @ManyToMany
    @JoinTable(name = "t_user_role")
    @Builder.Default // 结合 @Builder 使用，确保默认值被保留
    private Set<Role> roles = new HashSet<>(); // 集合字段

    // 针对测试，创建一个方便的自定义构造函数（不包含ID）
    public User(String username, Integer age, Department dept, Set<Role> roles) {
        this.username = username;
        this.age = age;
        this.dept = dept;
        this.roles = roles;
    }
}




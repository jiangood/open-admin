package io.admin.framework.data.specification;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 假设的 Role 实体
@Entity
@Table(name = "t_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private Long id;
    private String name;
}
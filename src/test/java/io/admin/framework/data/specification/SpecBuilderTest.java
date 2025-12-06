package io.admin.framework.data.specification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec<T> 动态查询构建器测试类
 * * 验证：基本查询、关联字段查询、模糊查询和集合查询 (isMember)。
 */
@DataJpaTest
public class SpecBuilderTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;

    private Department deptIT;
    private Department deptHR;
    private Role adminRole;
    private Role guestRole;

    @BeforeEach
    void setup() {
        // --- 1. 准备关联实体 (使用 Lombok 的全参构造函数) ---
        deptIT = new Department(1L, "IT");
        deptHR = new Department(2L, "HR");
        adminRole = new Role(100L, "ADMIN");
        guestRole = new Role(101L, "GUEST");
        
        // 必须先持久化依赖的实体
        entityManager.persist(deptIT);
        entityManager.persist(deptHR);
        entityManager.persist(adminRole);
        entityManager.persist(guestRole);

        // --- 2. 准备用户数据 (使用 Lombok @Builder 创建对象，更清晰) ---
        User userA = User.builder().username("Alice").age(25).dept(deptIT).roles(Set.of(adminRole)).build();
        User userB = User.builder().username("Bob").age(30).dept(deptIT).roles(Set.of(guestRole)).build();
        User userC = User.builder().username("Charlie").age(35).dept(deptHR).roles(Set.of(guestRole)).build();
        User userD = User.builder().username("David").age(40).dept(deptHR).build(); // roles 自动为空 HashSet

        userRepository.saveAll(List.of(userA, userB, userC, userD));
        entityManager.flush(); // 确保数据写入数据库
    }

    // --- 基础查询测试：AND 逻辑连接 ---
    
    @Test
    void testBasicEqualAndGreaterThan() {
        // 查找 age > 30 且 dept.id = 2 的用户 (Charlie, 35, HR)
        Specification<User> spec = Spec.<User>of()
                .greaterThan("age", 30) 
                .equal("dept.id", 2L);   

        List<User> results = userRepository.findAll(spec);
        
        assertThat(results).hasSize(2);
    }

    // --- 关联字段查询测试：使用点操作 ---

    @Test
    void testAssociatedFieldQuery() {
        // 查找部门名称是 'IT' 的用户 (Alice, Bob)
        Specification<User> spec = Spec.<User>of()
                .equal("dept.name", "IT"); 

        List<String> results = userRepository.findAll(spec).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        assertThat(results).containsExactlyInAnyOrder("Alice", "Bob");
    }
    
    // --- 模糊查询测试：不区分大小写和 OrLike ---

    @Test
    void testOrLikeAcrossMultipleFields() {
        // 查找 username 或 dept.name 包含 'i' 的用户
        Specification<User> spec = Spec.<User>of()
                .orLike("i", "username", "dept.name");

        List<String> names = userRepository.findAll(spec).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        // 修正：David 的 username 包含 'i'，故也应被找到。
        assertThat(names).containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David");
    }

    // --- 集合查询测试：isMember (IS MEMBER OF) ---

    @Test
    void testIsMemberQuery() {
        // 查找拥有 adminRole 的用户 (Alice)
        Specification<User> spec = Spec.<User>of()
                .isMember("roles", adminRole); // WHERE adminRole IS MEMBER OF roles

        List<String> results = userRepository.findAll(spec).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        assertThat(results).containsExactlyInAnyOrder("Alice");
    }

    @Test
    void testIsNotMemberQuery() {
        // 查找没有 guestRole 的用户 (Alice, David)
        Specification<User> spec = Spec.<User>of()
                .isNotMember("roles", guestRole); // WHERE guestRole IS NOT MEMBER OF roles

        List<String> results = userRepository.findAll(spec).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        // Alice 有 ADMIN，没有 GUEST； David 没有任何角色
        assertThat(results).containsExactlyInAnyOrder("Alice", "David");
    }
}
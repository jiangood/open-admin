package io.admin.framework.data.specification;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec<T> 动态查询构建器测试类
 * * 验证：基本查询、关联字段查询、模糊查询和集合查询 (isMember)。
 */
@DataJpaTest
@Import({UserDao.class, ValidationAutoConfiguration.class})
// 手动导入验证器的自动配置
public class SpecBuilderTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private EntityManager entityManager;


    @BeforeEach
    void setup() {

        // --- 2. 准备用户数据 (使用 Lombok @Builder 创建对象，更清晰) ---
        User userA = User.builder().username("Alice").age(20).build();
        User userB = User.builder().username("Bob").age(30).build();
        User userC = User.builder().username("Charlie").age(20).build();
        User userD = User.builder().username("Alice").age(40).build(); // roles 自动为空 HashSet

        userDao.saveAll(List.of(userA, userB, userC, userD));
        entityManager.flush(); // 确保数据写入数据库
    }

    @Test
    void testGroup() {
        Spec<User> spec = Spec.<User>of()

                .selectFnc(Spec.Fuc.SUM, "age")
                .selectFnc(Spec.Fuc.COUNT, "age")
                ;
        List<Object[]> age = userDao.stats(spec);


        for (Object[] row : age) {
            System.out.println(ReflectionToStringBuilder.toString(row));
        }
    }


}
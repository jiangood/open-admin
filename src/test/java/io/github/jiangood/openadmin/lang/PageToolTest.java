package io.github.jiangood.openadmin.lang;

import io.github.jiangood.openadmin.framework.data.PageExt;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PageTool工具类的单元测试
 */
class PageToolTest {

    // 测试用的原始类
    static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    // 测试用的转换后类
    static class UserDto {
        private String name;

        public UserDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    void testConvert() {
        // 创建测试数据
        List<User> users = List.of(
                new User("Alice", 20),
                new User("Bob", 25),
                new User("Charlie", 30)
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        // 测试转换功能
        Page<UserDto> convertedPage = PageTool.convert(page, user -> new UserDto(user.getName()));

        assertNotNull(convertedPage);
        assertEquals(3, convertedPage.getTotalElements());
        assertEquals(0, convertedPage.getNumber());
        assertEquals(10, convertedPage.getSize());
        
        List<UserDto> content = convertedPage.getContent();
        assertNotNull(content);
        assertEquals(3, content.size());
        assertEquals("Alice", content.get(0).getName());
        assertEquals("Bob", content.get(1).getName());
        assertEquals("Charlie", content.get(2).getName());
    }

    @Test
    void testAddSummary() {
        // 创建测试数据
        List<User> users = List.of(
                new User("Alice", 20),
                new User("Bob", 25)
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        // 测试添加summary
        Page<User> pageWithSummary = PageTool.addSummary(page, "Total users: 2");

        assertNotNull(pageWithSummary);
        assertTrue(pageWithSummary instanceof PageExt);
        
        PageExt<User> pageExt = (PageExt<User>) pageWithSummary;
        Map<String, Object> extData = pageExt.getExtData();
        assertNotNull(extData);
        assertEquals("Total users: 2", extData.get("summary"));
    }

    @Test
    void testAddExtraDataWithMap() {
        // 创建测试数据
        List<User> users = List.of(new User("Alice", 20));
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        // 测试添加额外数据（使用Map）
        Map<String, Object> extraData = Map.of(
                "totalAge", 20,
                "averageAge", 20.0
        );
        Page<User> pageWithExtraData = PageTool.addExtraData(page, extraData);

        assertNotNull(pageWithExtraData);
        assertTrue(pageWithExtraData instanceof PageExt);
        
        PageExt<User> pageExt = (PageExt<User>) pageWithExtraData;
        Map<String, Object> extData = pageExt.getExtData();
        assertNotNull(extData);
        assertEquals(2, extData.size());
        assertEquals(20, extData.get("totalAge"));
        assertEquals(20.0, extData.get("averageAge"));
    }

    @Test
    void testAddExtraDataWithKeyValue() {
        // 创建测试数据
        List<User> users = List.of(new User("Alice", 20));
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        // 测试添加额外数据（使用key-value）
        Page<User> pageWithExtraData = PageTool.addExtraData(page, "totalUsers", 1);

        assertNotNull(pageWithExtraData);
        assertTrue(pageWithExtraData instanceof PageExt);
        
        PageExt<User> pageExt = (PageExt<User>) pageWithExtraData;
        Map<String, Object> extData = pageExt.getExtData();
        assertNotNull(extData);
        assertEquals(1, extData.size());
        assertEquals(1, extData.get("totalUsers"));

        // 测试在已有的PageExt上添加额外数据
        Page<User> pageWithMoreExtraData = PageTool.addExtraData(pageWithExtraData, "averageAge", 20.0);
        PageExt<User> pageExt2 = (PageExt<User>) pageWithMoreExtraData;
        Map<String, Object> extData2 = pageExt2.getExtData();
        assertNotNull(extData2);
        assertEquals(2, extData2.size());
        assertEquals(1, extData2.get("totalUsers"));
        assertEquals(20.0, extData2.get("averageAge"));
    }

    @Test
    void testGetExt() {
        // 测试1：传入普通Page，应该转换为PageExt
        List<User> users = List.of(new User("Alice", 20));
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        PageExt<User> pageExt1 = PageTool.getExt(page);
        assertNotNull(pageExt1);
        assertTrue(pageExt1 instanceof PageExt);
        assertEquals(1, pageExt1.getTotalElements());

        // 测试2：传入PageExt，应该直接返回
        PageExt<User> originalPageExt = PageExt.of(page);
        originalPageExt.putExtData("testKey", "testValue");

        PageExt<User> pageExt2 = PageTool.getExt(originalPageExt);
        assertNotNull(pageExt2);
        assertSame(originalPageExt, pageExt2); // 应该是同一个对象
        assertEquals("testValue", pageExt2.getExtData().get("testKey"));
    }
}

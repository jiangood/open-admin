package io.github.jiangood.as.framework.data.converter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ToListConverterTest {

    private final ToListConverter converter = new ToListConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        // 测试正常列表转换
        List<String> list = Arrays.asList("item1", "item2", "item3");
        String result = converter.convertToDatabaseColumn(list);
        assertEquals("item1,item2,item3", result);

        // 测试空列表
        List<String> emptyList = new ArrayList<>();
        String emptyResult = converter.convertToDatabaseColumn(emptyList);
        assertEquals("", emptyResult);

        // 测试null列表
        String nullResult = converter.convertToDatabaseColumn(null);
        assertNull(nullResult);

        // 测试包含空字符串的列表
        List<String> listWithEmpty = Arrays.asList("item1", "", "item3");
        String resultWithEmpty = converter.convertToDatabaseColumn(listWithEmpty);
        assertEquals("item1,,item3", resultWithEmpty);

        // 测试包含逗号的字符串
        List<String> listWithComma = Arrays.asList("item1", "item,with,comma", "item3");
        String resultWithComma = converter.convertToDatabaseColumn(listWithComma);
        assertEquals("item1,item,with,comma,item3", resultWithComma);
    }

    @Test
    public void testConvertToEntityAttribute() {
        // 测试正常字符串转换
        String dbData = "item1,item2,item3";
        List<String> result = converter.convertToEntityAttribute(dbData);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));

        // 测试空字符串
        List<String> emptyResult = converter.convertToEntityAttribute("");
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());

        // 测试null字符串
        List<String> nullResult = converter.convertToEntityAttribute(null);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        // 测试没有逗号的单个值
        String singleValue = "singleItem";
        List<String> singleResult = converter.convertToEntityAttribute(singleValue);
        assertNotNull(singleResult);
        assertEquals(1, singleResult.size());
        assertEquals("singleItem", singleResult.get(0));

        // 测试包含空值的字符串
        String withEmptyValue = "item1,,item3";
        List<String> resultWithEmpty = converter.convertToEntityAttribute(withEmptyValue);
        assertNotNull(resultWithEmpty);
        assertEquals(3, resultWithEmpty.size());
        assertEquals("item1", resultWithEmpty.get(0));
        assertEquals("", resultWithEmpty.get(1));
        assertEquals("item3", resultWithEmpty.get(2));
    }

    @Test
    public void testRoundTripConversion() {
        // 测试从列表转换到数据库再转换回列表
        List<String> originalList = Arrays.asList("test1", "test2", "test3");

        // 转换为数据库字符串
        String dbString = converter.convertToDatabaseColumn(originalList);
        assertEquals("test1,test2,test3", dbString);

        // 从数据库字符串转换回列表
        List<String> convertedList = converter.convertToEntityAttribute(dbString);

        // 验证列表内容一致
        assertNotNull(convertedList);
        assertEquals(originalList.size(), convertedList.size());
        assertEquals(originalList, convertedList);
    }
}

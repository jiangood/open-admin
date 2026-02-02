package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import jakarta.persistence.RollbackException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ExceptionToMessageTool工具类的单元测试
 */
class ExceptionToMessageToolTest {

    @Test
    void testConvertWithNull() {
        // 测试null值
        String message = ExceptionToMessageTool.convert(null);
        assertNull(message);
    }

    @Test
    void testConvertWithGeneralException() {
        // 测试一般异常（无中文消息）
        Exception exception = new Exception("Internal server error");
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("服务器忙", message);
    }

    @Test
    void testConvertWithChineseException() {
        // 测试包含中文的异常
        Exception exception = new Exception("测试异常消息");
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("测试异常消息", message);
    }

    @Test
    void testConvertWithDataIntegrityViolationException() {
        // 测试数据完整性异常 - 数据长度超过限制
        SQLIntegrityConstraintViolationException sqlException = new SQLIntegrityConstraintViolationException("Data too long for column 'name'");
        Exception causeException = new Exception(sqlException);
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation", causeException);
        
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("数据长度超过限制，请修改！", message);
    }

    @Test
    void testConvertWithDuplicateDataException() {
        // 测试数据完整性异常 - 数据重复
        SQLIntegrityConstraintViolationException sqlException = new SQLIntegrityConstraintViolationException("Duplicate entry 'test' for key 'name'");
        Exception causeException = new Exception(sqlException);
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation", causeException);
        
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("数据【test】重复", message);
    }

    @Test
    void testConvertWithNullColumnException() {
        // 测试数据完整性异常 - 列不能为空
        SQLIntegrityConstraintViolationException sqlException = new SQLIntegrityConstraintViolationException("Column 'name' cannot be null");
        Exception causeException = new Exception(sqlException);
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation", causeException);
        
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("字段name不能为空", message);
    }

    @Test
    void testConvertWithGenericDataIntegrityViolationException() {
        // 测试一般数据完整性异常
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation");
        
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("数据已被引用，请检查", message);
    }

    @Test
    void testConvertWithTransactionSystemException() {
        // 测试事务系统异常 - 不包含ConstraintViolationException
        TransactionSystemException exception = new TransactionSystemException("Transaction system exception");
        
        String message = ExceptionToMessageTool.convert(exception);
        assertNotNull(message);
        assertEquals("服务器忙", message);
    }


}

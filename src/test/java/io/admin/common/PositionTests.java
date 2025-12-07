package io.admin.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTests {

    @Test
    void testNoArgsConstructor() {
        Position position = new Position();
        
        assertNull(position.getX());
        assertNull(position.getY());
    }

    @Test
    void testAllArgsConstructor() {
        Position position = new Position(10, 20);
        
        assertEquals(Integer.valueOf(10), position.getX());
        assertEquals(Integer.valueOf(20), position.getY());
    }

    @Test
    void testGettersAndSetters() {
        Position position = new Position();
        
        position.setX(5);
        position.setY(15);
        
        assertEquals(Integer.valueOf(5), position.getX());
        assertEquals(Integer.valueOf(15), position.getY());
        
        // 修改值
        position.setX(100);
        position.setY(200);
        
        assertEquals(Integer.valueOf(100), position.getX());
        assertEquals(Integer.valueOf(200), position.getY());
    }
}
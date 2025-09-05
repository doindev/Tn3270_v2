package org.me.io;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CircularByteBufferTest {
    
    private CircularByteBuffer buffer;
    
    @Before
    public void setUp() {
        buffer = new CircularByteBuffer(10);
    }
    
    @Test
    public void testConstructorWithDefaultSize() {
        CircularByteBuffer defaultBuffer = new CircularByteBuffer();
        assertEquals(CircularByteBuffer.DEFAULT_BUFFER_SIZE, defaultBuffer.getSpace());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSize() {
        new CircularByteBuffer(0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeSize() {
        new CircularByteBuffer(-1);
    }
    
    @Test
    public void testAddSingleByte() {
        buffer.add((byte) 1);
        assertEquals(1, buffer.getCurrentNumberOfBytes());
        assertEquals(9, buffer.getSpace());
        assertTrue(buffer.hasBytes());
        assertTrue(buffer.hasSpace());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAddToFullBuffer() {
        for (int i = 0; i < 10; i++) {
            buffer.add((byte) i);
        }
        assertFalse(buffer.hasSpace());
        buffer.add((byte) 11);
    }
    
    @Test
    public void testAddByteArray() {
        byte[] data = {1, 2, 3, 4, 5};
        buffer.add(data, 0, 5);
        assertEquals(5, buffer.getCurrentNumberOfBytes());
    }
    
    @Test(expected = NullPointerException.class)
    public void testAddNullArray() {
        buffer.add(null, 0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithInvalidOffset() {
        byte[] data = {1, 2, 3};
        buffer.add(data, -1, 2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithInvalidLength() {
        byte[] data = {1, 2, 3};
        buffer.add(data, 0, 5);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAddArrayToFullBuffer() {
        byte[] data = new byte[11];
        buffer.add(data, 0, 11);
    }
    
    @Test
    public void testClear() {
        buffer.add((byte) 1);
        buffer.add((byte) 2);
        assertEquals(2, buffer.getCurrentNumberOfBytes());
        
        buffer.clear();
        assertEquals(0, buffer.getCurrentNumberOfBytes());
        assertEquals(10, buffer.getSpace());
        assertFalse(buffer.hasBytes());
    }
    
    @Test
    public void testReadSingleByte() {
        buffer.add((byte) 42);
        byte result = buffer.read();
        assertEquals(42, result);
        assertEquals(0, buffer.getCurrentNumberOfBytes());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testReadFromEmptyBuffer() {
        buffer.read();
    }
    
    @Test
    public void testReadByteArray() {
        byte[] data = {1, 2, 3, 4, 5};
        buffer.add(data, 0, 5);
        
        byte[] result = new byte[3];
        buffer.read(result, 0, 3);
        
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
        assertEquals(2, buffer.getCurrentNumberOfBytes());
    }
    
    @Test(expected = NullPointerException.class)
    public void testReadToNullArray() {
        buffer.add((byte) 1);
        buffer.read(null, 0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testReadWithInvalidOffset() {
        buffer.add((byte) 1);
        byte[] result = new byte[3];
        buffer.read(result, -1, 1);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testReadMoreThanAvailable() {
        buffer.add((byte) 1);
        byte[] result = new byte[5];
        buffer.read(result, 0, 5);
    }
    
    @Test
    public void testPeek() {
        byte[] data = {1, 2, 3, 4, 5};
        buffer.add(data, 0, 5);
        
        byte[] result = new byte[3];
        assertTrue(buffer.peek(result, 0, 3));
        
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
        assertEquals(5, buffer.getCurrentNumberOfBytes());
    }
    
    @Test
    public void testPeekNotEnoughBytes() {
        buffer.add((byte) 1);
        byte[] result = new byte[5];
        assertFalse(buffer.peek(result, 0, 5));
    }
    
    @Test(expected = NullPointerException.class)
    public void testPeekNullArray() {
        buffer.peek(null, 0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPeekInvalidOffset() {
        byte[] result = new byte[3];
        buffer.peek(result, -1, 1);
    }
    
    @Test
    public void testHasSpace() {
        assertTrue(buffer.hasSpace());
        assertTrue(buffer.hasSpace(10));
        assertFalse(buffer.hasSpace(11));
        
        for (int i = 0; i < 10; i++) {
            buffer.add((byte) i);
        }
        
        assertFalse(buffer.hasSpace());
        assertFalse(buffer.hasSpace(1));
    }
    
    @Test
    public void testCircularBehavior() {
        for (int i = 0; i < 10; i++) {
            buffer.add((byte) i);
        }
        
        for (int i = 0; i < 5; i++) {
            buffer.read();
        }
        
        for (int i = 10; i < 15; i++) {
            buffer.add((byte) i);
        }
        
        byte[] result = new byte[10];
        buffer.read(result, 0, 10);
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 5, result[i]);
        }
        for (int i = 5; i < 10; i++) {
            assertEquals(i + 5, result[i]);
        }
    }
    
    @Test
    public void testMultipleWriteReadCycles() {
        for (int cycle = 0; cycle < 3; cycle++) {
            byte[] writeData = new byte[8];
            for (int i = 0; i < 8; i++) {
                writeData[i] = (byte) (cycle * 10 + i);
            }
            buffer.add(writeData, 0, 8);
            
            byte[] readData = new byte[8];
            buffer.read(readData, 0, 8);
            
            for (int i = 0; i < 8; i++) {
                assertEquals(writeData[i], readData[i]);
            }
        }
    }
}
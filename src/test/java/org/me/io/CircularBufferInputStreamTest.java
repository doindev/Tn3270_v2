package org.me.io;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CircularBufferInputStreamTest {
    
    private CircularBufferInputStream stream;
    private byte[] testData;
    
    @Before
    public void setUp() {
        testData = new byte[100];
        for (int i = 0; i < 100; i++) {
            testData[i] = (byte) i;
        }
    }
    
    @Test
    public void testConstructorWithDefaultBufferSize() {
        InputStream in = new ByteArrayInputStream(testData);
        CircularBufferInputStream cbis = new CircularBufferInputStream(in);
        assertNotNull(cbis);
    }
    
    @Test
    public void testConstructorWithCustomBufferSize() {
        InputStream in = new ByteArrayInputStream(testData);
        CircularBufferInputStream cbis = new CircularBufferInputStream(in, 50);
        assertNotNull(cbis);
    }
    
    @Test
    public void testReadSingleByte() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        
        int result = stream.read();
        assertEquals(0, result);
        
        result = stream.read();
        assertEquals(1, result);
    }
    
    @Test
    public void testReadEndOfStream() throws IOException {
        byte[] smallData = {1, 2, 3};
        InputStream in = new ByteArrayInputStream(smallData);
        stream = new CircularBufferInputStream(in, 10);
        
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
    }
    
    @Test
    public void testReadByteArray() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 20);
        
        byte[] buffer = new byte[10];
        int bytesRead = stream.read(buffer, 0, 10);
        
        assertEquals(10, bytesRead);
        for (int i = 0; i < 10; i++) {
            assertEquals(i, buffer[i]);
        }
    }
    
    @Test
    public void testReadByteArrayPartial() throws IOException {
        byte[] smallData = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(smallData);
        stream = new CircularBufferInputStream(in, 10);
        
        byte[] buffer = new byte[10];
        int bytesRead = stream.read(buffer, 0, 10);
        
        assertEquals(5, bytesRead);
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, buffer[i]);
        }
    }
    
    @Test
    public void testReadByteArrayEndOfStream() throws IOException {
        byte[] smallData = {1, 2, 3};
        InputStream in = new ByteArrayInputStream(smallData);
        stream = new CircularBufferInputStream(in, 10);
        
        byte[] buffer = new byte[10];
        stream.read(buffer, 0, 3);
        
        int bytesRead = stream.read(buffer, 0, 10);
        assertEquals(-1, bytesRead);
    }
    
    @Test(expected = NullPointerException.class)
    public void testReadNullArray() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        stream.read(null, 0, 5);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        byte[] buffer = new byte[10];
        stream.read(buffer, -1, 5);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidLength() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        byte[] buffer = new byte[10];
        stream.read(buffer, 0, 15);
    }
    
    @Test
    public void testReadZeroLength() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        byte[] buffer = new byte[10];
        int bytesRead = stream.read(buffer, 0, 0);
        assertEquals(0, bytesRead);
    }
    
    @Test
    public void testBufferFilling() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        
        byte[] buffer = new byte[30];
        int bytesRead = stream.read(buffer, 0, 30);
        
        assertEquals(30, bytesRead);
        for (int i = 0; i < 30; i++) {
            assertEquals(i, buffer[i]);
        }
    }
    
    @Test
    public void testClose() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 10);
        
        stream.read();
        stream.close();
        
        assertThrows(IOException.class, () -> stream.read());
    }
    
    @Test
    public void testMultipleReads() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 20);
        
        byte[] buffer1 = new byte[15];
        byte[] buffer2 = new byte[15];
        byte[] buffer3 = new byte[15];
        
        assertEquals(15, stream.read(buffer1, 0, 15));
        assertEquals(15, stream.read(buffer2, 0, 15));
        assertEquals(15, stream.read(buffer3, 0, 15));
        
        for (int i = 0; i < 15; i++) {
            assertEquals(i, buffer1[i]);
            assertEquals(i + 15, buffer2[i]);
            assertEquals(i + 30, buffer3[i]);
        }
    }
    
    @Test
    public void testHaveBytes() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 20);
        
        int available = stream.haveBytes(10);
        assertTrue(available >= 10);
    }
    
    @Test
    public void testLargeDataStreaming() throws IOException {
        byte[] largeData = new byte[10000];
        for (int i = 0; i < 10000; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        InputStream in = new ByteArrayInputStream(largeData);
        stream = new CircularBufferInputStream(in, 100);
        
        byte[] buffer = new byte[1000];
        int totalBytesRead = 0;
        int bytesRead;
        
        while ((bytesRead = stream.read(buffer, 0, 1000)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                assertEquals((byte) ((totalBytesRead + i) % 256), buffer[i]);
            }
            totalBytesRead += bytesRead;
        }
        
        assertEquals(10000, totalBytesRead);
    }
    
    @Test
    public void testReadAfterPartialFill() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new CircularBufferInputStream(in, 50);
        
        for (int i = 0; i < 100; i++) {
            int result = stream.read();
            assertEquals(i, result);
        }
        
        assertEquals(-1, stream.read());
    }
}
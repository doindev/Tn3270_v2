package org.me.io;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PeekableInputStreamTest {
    
    private PeekableInputStream stream;
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
        PeekableInputStream pis = new PeekableInputStream(in);
        assertNotNull(pis);
    }
    
    @Test
    public void testConstructorWithCustomBufferSize() {
        InputStream in = new ByteArrayInputStream(testData);
        PeekableInputStream pis = new PeekableInputStream(in, 50);
        assertNotNull(pis);
    }
    
    @Test
    public void testPeekSingleByte() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[1];
        assertTrue(stream.peek(peekBuffer, 0, 1));
        assertEquals(0, peekBuffer[0]);
        
        int readValue = stream.read();
        assertEquals(0, readValue);
    }
    
    @Test
    public void testPeekMultipleBytes() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[5];
        assertTrue(stream.peek(peekBuffer, 0, 5));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, peekBuffer[i]);
        }
        
        byte[] readBuffer = new byte[5];
        stream.read(readBuffer, 0, 5);
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, readBuffer[i]);
        }
    }
    
    @Test
    public void testPeekWithFullArray() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[10];
        assertTrue(stream.peek(peekBuffer));
        
        for (int i = 0; i < 10; i++) {
            assertEquals(i, peekBuffer[i]);
        }
    }
    
    @Test
    public void testPeekDoesNotConsumeData() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer1 = new byte[5];
        byte[] peekBuffer2 = new byte[5];
        
        assertTrue(stream.peek(peekBuffer1, 0, 5));
        assertTrue(stream.peek(peekBuffer2, 0, 5));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(peekBuffer1[i], peekBuffer2[i]);
        }
        
        int readValue = stream.read();
        assertEquals(0, readValue);
    }
    
    @Test
    public void testPeekMoreThanAvailable() throws IOException {
        byte[] smallData = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(smallData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[10];
        assertFalse(stream.peek(peekBuffer, 0, 10));
        
        assertTrue(stream.peek(peekBuffer, 0, 5));
    }
    
    @Test
    public void testPeekWithOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[10];
        assertTrue(stream.peek(peekBuffer, 2, 5));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, peekBuffer[i + 2]);
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void testPeekNullArray() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        stream.peek(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testPeekNullArrayWithOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        stream.peek(null, 0, 5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPeekInvalidOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        byte[] peekBuffer = new byte[10];
        stream.peek(peekBuffer, -1, 5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPeekInvalidLength() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        byte[] peekBuffer = new byte[10];
        stream.peek(peekBuffer, 0, 15);
    }
    
    @Test
    public void testPeekThenReadSequence() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[10];
        assertTrue(stream.peek(peekBuffer, 0, 10));
        
        for (int i = 0; i < 10; i++) {
            int readValue = stream.read();
            assertEquals(peekBuffer[i], (byte) readValue);
        }
    }
    
    @Test
    public void testMultiplePeeksWithDifferentSizes() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 30);
        
        byte[] peek1 = new byte[5];
        byte[] peek2 = new byte[10];
        byte[] peek3 = new byte[15];
        
        assertTrue(stream.peek(peek1, 0, 5));
        assertTrue(stream.peek(peek2, 0, 10));
        assertTrue(stream.peek(peek3, 0, 15));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, peek1[i]);
            assertEquals(i, peek2[i]);
            assertEquals(i, peek3[i]);
        }
        
        for (int i = 5; i < 10; i++) {
            assertEquals(i, peek2[i]);
            assertEquals(i, peek3[i]);
        }
        
        for (int i = 10; i < 15; i++) {
            assertEquals(i, peek3[i]);
        }
    }
    
    @Test
    public void testPeekAfterPartialRead() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] readBuffer = new byte[5];
        stream.read(readBuffer, 0, 5);
        
        byte[] peekBuffer = new byte[5];
        assertTrue(stream.peek(peekBuffer, 0, 5));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 5, peekBuffer[i]);
        }
    }
    
    @Test
    public void testPeekAtEndOfStream() throws IOException {
        byte[] smallData = {1, 2, 3};
        InputStream in = new ByteArrayInputStream(smallData);
        stream = new PeekableInputStream(in, 10);
        
        byte[] readBuffer = new byte[3];
        stream.read(readBuffer, 0, 3);
        
        byte[] peekBuffer = new byte[1];
        assertFalse(stream.peek(peekBuffer, 0, 1));
    }
    
    @Test
    public void testPeekWithLargeBuffer() throws IOException {
        byte[] largeData = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        InputStream in = new ByteArrayInputStream(largeData);
        stream = new PeekableInputStream(in, 500);
        
        byte[] peekBuffer = new byte[400];
        assertTrue(stream.peek(peekBuffer, 0, 400));
        
        for (int i = 0; i < 400; i++) {
            assertEquals((byte) (i % 256), peekBuffer[i]);
        }
        
        byte[] readBuffer = new byte[400];
        stream.read(readBuffer, 0, 400);
        
        for (int i = 0; i < 400; i++) {
            assertEquals(peekBuffer[i], readBuffer[i]);
        }
    }
    
    @Test
    public void testInheritedReadMethods() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        assertEquals(0, stream.read());
        
        byte[] buffer = new byte[10];
        assertEquals(10, stream.read(buffer, 0, 10));
        
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, buffer[i]);
        }
    }
    
    @Test
    public void testCloseAfterPeek() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[5];
        assertTrue(stream.peek(peekBuffer, 0, 5));
        
        stream.close();
        
        assertThrows(IOException.class, () -> stream.peek(peekBuffer, 0, 5));
    }
    
    @Test
    public void testComplexScenario() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 30);
        
        byte[] peek1 = new byte[10];
        assertTrue(stream.peek(peek1, 0, 10));
        
        byte[] read1 = new byte[5];
        assertEquals(5, stream.read(read1, 0, 5));
        
        for (int i = 0; i < 5; i++) {
            assertEquals(i, read1[i]);
        }
        
        byte[] peek2 = new byte[10];
        assertTrue(stream.peek(peek2, 0, 10));
        
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 5, peek2[i]);
        }
        
        for (int i = 5; i < 15; i++) {
            assertEquals(i, stream.read());
        }
        
        byte[] peek3 = new byte[20];
        assertTrue(stream.peek(peek3, 0, 20));
        
        for (int i = 0; i < 20; i++) {
            assertEquals(i + 15, peek3[i]);
        }
    }
    
    @Test
    public void testPeekWithZeroLength() throws IOException {
        InputStream in = new ByteArrayInputStream(testData);
        stream = new PeekableInputStream(in, 20);
        
        byte[] peekBuffer = new byte[10];
        assertTrue(stream.peek(peekBuffer, 0, 0));
    }
}
package org.me.telnet.tn3270;

import org.junit.Test;
import org.me.io.PeekableInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class IacEorHandlingTest {
    
    @Test
    public void testIacEorTerminatesDataStream() throws IOException {
        // Create a screen buffer
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Create a data stream that includes IAC,EOR at the end
        byte[] dataStream = new byte[] {
            (byte) 0x05,  // Will be corrected to 0xF5 (CMD_ERASE_WRITE)
            (byte) 0x02,  // WCC (keyboard restore)
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute (unprotected)
            (byte) 0xC8, (byte) 0x85, (byte) 0x93, (byte) 0x93, (byte) 0x96,  // "Hello" in EBCDIC
            (byte) 0xFF,  // IAC
            (byte) 0xEF   // EOR (239 = 0xEF)
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        // Parse the data stream
        parser.parse(stream);
        
        // The parser should have processed everything up to IAC,EOR
        // and stopped there
        
        // Check that field was created
        InputField[] fields = buffer.getFields().toArray(new InputField[0]);
        assertEquals("Should have 1 field", 1, fields.length);
        
        // Check that "Hello" was written to the buffer
        String screenContent = buffer.getRowAsString(1);
        assertTrue("Screen should contain 'Hello'", screenContent.contains("Hello"));
        
        // Verify that no more data is read after IAC,EOR
        // Stream should have no more data
        byte[] remaining = new byte[10];
        int read = stream.read(remaining);
        assertEquals("No data should remain after IAC,EOR", -1, read);
    }
    
    @Test
    public void testMultipleDataStreamsWithIacEor() throws IOException {
        // Test that multiple data streams separated by IAC,EOR are handled correctly
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Create two data streams separated by IAC,EOR
        byte[] dataStream = new byte[] {
            // First data stream
            (byte) 0x05,  // Will be corrected to 0xF5 (CMD_ERASE_WRITE)
            (byte) 0x02,  // WCC
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute
            (byte) 0xF1,  // "1" in EBCDIC
            (byte) 0xFF,  // IAC
            (byte) 0xEF,  // EOR
            
            // Second data stream
            (byte) 0x01,  // Will be corrected to 0xF1 (CMD_WRITE)
            (byte) 0x02,  // WCC
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute
            (byte) 0xF2,  // "2" in EBCDIC
            (byte) 0xFF,  // IAC
            (byte) 0xEF   // EOR
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        // Parse first data stream
        parser.parse(stream);
        
        // Check first result
        String screenContent = buffer.getRowAsString(1);
        assertTrue("First parse should show '1'", screenContent.contains("1"));
        
        // Parse second data stream
        parser.parse(stream);
        
        // Check second result - note that CMD_WRITE doesn't clear the screen
        screenContent = buffer.getRowAsString(1);
        assertTrue("Second parse should show '2'", screenContent.contains("2"));
    }
    
    @Test
    public void testIacNotFollowedByEor() throws IOException {
        // Test that IAC followed by non-EOR byte is handled correctly
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Create a data stream with IAC followed by a regular byte (not EOR)
        byte[] dataStream = new byte[] {
            (byte) 0x05,  // Will be corrected to 0xF5 (CMD_ERASE_WRITE)
            (byte) 0x02,  // WCC
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute
            (byte) 0xFF,  // IAC (255) - should be processed as character data since not followed by EOR
            (byte) 0xF3,  // "3" in EBCDIC (not EOR)
            (byte) 0xFF,  // IAC
            (byte) 0xEF   // EOR - actual end marker
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        // Parse the data stream
        parser.parse(stream);
        
        // The IAC at position 4 should be treated as data since it's not followed by EOR
        // However, the final IAC,EOR should terminate the stream properly
        InputField[] fields = buffer.getFields().toArray(new InputField[0]);
        assertEquals("Should have 1 field", 1, fields.length);
    }
}
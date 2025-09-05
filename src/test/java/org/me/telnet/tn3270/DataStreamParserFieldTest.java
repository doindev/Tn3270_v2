package org.me.telnet.tn3270;

import org.junit.Test;
import org.me.io.PeekableInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class DataStreamParserFieldTest {
    
    @Test
    public void testDeferredFieldCreation() throws IOException {
        // Create a screen buffer
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Create a data stream that includes:
        // CMD_ERASE_WRITE + WCC + SF order + attribute + text
        byte[] dataStream = new byte[] {
            (byte) 0xF5,  // CMD_ERASE_WRITE
            (byte) 0x02,  // WCC (keyboard restore)
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute (unprotected)
            (byte) 0xC8, (byte) 0x85, (byte) 0x93, (byte) 0x93, (byte) 0x96,  // "Hello" in EBCDIC
            (byte) 0x1D,  // ORDER_START_FIELD (second field)
            (byte) 0x60,  // Field attribute (protected)
            (byte) 0xE6, (byte) 0x96, (byte) 0x99, (byte) 0x93, (byte) 0x84  // "World" in EBCDIC
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        // Parse the data stream
        parser.parse(stream);
        
        // After parsing, fields should have been created
        InputField[] fields = buffer.getFields().toArray(new InputField[0]);
        
        // We should have 2 fields
        assertEquals("Should have 2 fields", 2, fields.length);
        
        // First field should be unprotected
        InputField field1 = fields[0];
        assertTrue("First field should be unprotected", field1.canInput());
        
        // Check that first field starts at position 1 (after the attribute byte at position 0)
        assertEquals("First field data should start at row 1", 1, field1.startRow());
        assertEquals("First field data should start at column 2", 2, field1.startColumn());
        
        // Second field should be protected
        InputField field2 = fields[1];
        assertFalse("Second field should be protected", field2.canInput());
        
        // Verify field boundaries are correct
        // First field should end where second field's attribute starts
        assertEquals("First field should end at column 6", 6, field1.endColumn());
        
        // Second field data should start after its attribute
        assertEquals("Second field data should start at column 8", 8, field2.startColumn());
    }
    
    @Test
    public void testFieldDataExtraction() throws IOException {
        // Create a screen buffer
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Create a data stream with field and data
        byte[] dataStream = new byte[] {
            (byte) 0xF5,  // CMD_ERASE_WRITE
            (byte) 0x02,  // WCC
            (byte) 0x11,  // ORDER_SET_BUFFER_ADDRESS
            (byte) 0x40, (byte) 0x40,  // Address 0,0
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute (unprotected)
            (byte) 0xE3, (byte) 0x85, (byte) 0xA2, (byte) 0xA3,  // "Test" in EBCDIC
            (byte) 0x11,  // ORDER_SET_BUFFER_ADDRESS
            (byte) 0x40, (byte) 0x50,  // Address for next field
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x60   // Field attribute (protected)
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        // Parse the data stream
        parser.parse(stream);
        
        // Check that field contains the expected data
        InputField[] fields = buffer.getFields().toArray(new InputField[0]);
        assertTrue("Should have at least 1 field", fields.length >= 1);
        
        // First field should contain "Test" (after EBCDIC conversion)
        InputField field = fields[0];
        String fieldData = field.getDataTrimmed();
        assertEquals("Field should contain 'Test'", "Test", fieldData);
    }
    
    @Test
    public void testFieldBoundariesWithWraparound() throws IOException {
        // Test when a field wraps around from end to beginning of screen
        ScreenBuffer buffer = new ScreenBuffer(2, 10); // Small buffer for testing
        DataStreamParser parser = new DataStreamParser(buffer);
        
        // Place field attribute near end of buffer
        byte[] dataStream = new byte[] {
            (byte) 0xF5,  // CMD_ERASE_WRITE
            (byte) 0x02,  // WCC
            (byte) 0x11,  // ORDER_SET_BUFFER_ADDRESS
            (byte) 0x40, (byte) 0x52,  // Address 18 (row 2, col 9 in 2x10 buffer)
            (byte) 0x1D,  // ORDER_START_FIELD
            (byte) 0x40,  // Field attribute
            // Add some data that would wrap
            (byte) 0xC1, (byte) 0xC2, (byte) 0xC3  // "ABC" in EBCDIC
        };
        
        ByteArrayInputStream bis = new ByteArrayInputStream(dataStream);
        PeekableInputStream stream = new PeekableInputStream(bis);
        
        parser.parse(stream);
        
        InputField[] fields = buffer.getFields().toArray(new InputField[0]);
        assertEquals("Should have 1 field", 1, fields.length);
        
        InputField field = fields[0];
        // Field data starts at position 19 (after attribute at 18)
        // In 2x10 buffer, position 19 is row 2, col 10
        assertEquals("Field should start at row 2", 2, field.startRow());
        assertEquals("Field should start at column 10", 10, field.startColumn());
        
        // With no other field, it should wrap around to just before itself
        // The field would extend from position 19 through the buffer wrap to position 17
    }
}
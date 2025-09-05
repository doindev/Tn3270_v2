package org.me.telnet.tn3270;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScreenPutStringTest {
    
    @Test
    public void testPutStringWritesToBuffer() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Create a field at position (0,10) that spans to (0,30)
        FieldAttribute attr = new FieldAttribute((byte) 0x40); // Unprotected field
        InputField field = new InputField(0, 10, 0, 30, attr, 24, 80);
        buffer.addField(field);
        
        // Position cursor in the field
        buffer.setCursorPosition(0, 10);
        
        // Write a string
        screen.putString("Hello World");
        
        // Verify the string was written to the buffer
        String row = buffer.getRowAsString(1); // Row 1 (0-indexed internally)
        assertTrue("Buffer should contain 'Hello World'", row.contains("Hello World"));
        
        // Verify cursor moved forward
        assertEquals("Cursor should be at column 21", 21, buffer.getCursorColumn());
    }
    
    @Test
    public void testPutStringRespectsFieldBoundaries() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Create a small field from (0,5) to (0,10) - 6 characters
        FieldAttribute attr1 = new FieldAttribute((byte) 0x40); // Unprotected
        InputField field1 = new InputField(0, 5, 0, 10, attr1, 24, 80);
        buffer.addField(field1);
        
        // Create another field from (0,20) to (0,30) 
        FieldAttribute attr2 = new FieldAttribute((byte) 0x40); // Unprotected
        InputField field2 = new InputField(0, 20, 0, 30, attr2, 24, 80);
        buffer.addField(field2);
        
        // Position cursor in first field
        buffer.setCursorPosition(0, 5);
        
        // Try to write a string longer than the field
        screen.putString("1234567890ABCDEF"); // 16 characters
        
        // First field should contain only "123456" (6 chars)
        String fieldContent = "";
        for (int col = 5; col <= 10; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertEquals("First field should contain '123456'", "123456", fieldContent.trim());
        
        // Second field should contain the overflow "7890AB..."
        fieldContent = "";
        for (int col = 20; col <= 30; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertTrue("Second field should contain overflow text", fieldContent.trim().startsWith("7890AB"));
    }
    
    @Test
    public void testPutStringSkipsProtectedFields() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Create an unprotected field
        FieldAttribute attr1 = new FieldAttribute((byte) 0x40); // Unprotected
        InputField field1 = new InputField(0, 5, 0, 10, attr1, 24, 80);
        buffer.addField(field1);
        
        // Create a protected field
        FieldAttribute attr2 = new FieldAttribute((byte) 0x60); // Protected
        InputField field2 = new InputField(0, 15, 0, 20, attr2, 24, 80);
        buffer.addField(field2);
        
        // Create another unprotected field
        FieldAttribute attr3 = new FieldAttribute((byte) 0x40); // Unprotected
        InputField field3 = new InputField(0, 25, 0, 35, attr3, 24, 80);
        buffer.addField(field3);
        
        // Position cursor in first field
        buffer.setCursorPosition(0, 5);
        
        // Write text that would overflow into protected field
        screen.putString("123456ABCDEFGHIJ"); // Should skip protected field
        
        // First field should have "123456"
        String fieldContent = "";
        for (int col = 5; col <= 10; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertEquals("First field should contain '123456'", "123456", fieldContent.trim());
        
        // Protected field should be empty
        fieldContent = "";
        for (int col = 15; col <= 20; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertEquals("Protected field should be empty", "      ", fieldContent);
        
        // Third field should contain the overflow
        fieldContent = "";
        for (int col = 25; col <= 35; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertTrue("Third field should contain overflow", fieldContent.trim().startsWith("ABCDEF"));
    }
    
    @Test
    public void testPutStringWithNumericField() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Create a numeric field
        FieldAttribute attr = new FieldAttribute((byte) 0x50); // Numeric unprotected
        InputField field = new InputField(0, 5, 0, 15, attr, 24, 80);
        buffer.addField(field);
        
        // Position cursor in the field
        buffer.setCursorPosition(0, 5);
        
        // Try to write mixed alphanumeric text
        screen.putString("ABC123DEF456");
        
        // Only numbers should be written
        String fieldContent = "";
        for (int col = 5; col <= 15; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        assertEquals("Numeric field should contain only numbers", "123456     ", fieldContent);
    }
    
    @Test
    public void testPutStringDoesNotOverwriteFieldMarkers() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Set field attributes at specific positions
        FieldAttribute attr1 = new FieldAttribute((byte) 0x40);
        buffer.setAttribute(0, 5, attr1); // Field marker at (0,5)
        
        FieldAttribute attr2 = new FieldAttribute((byte) 0x40);
        buffer.setAttribute(0, 10, attr2); // Field marker at (0,10)
        
        // Create input fields based on these markers
        InputField field1 = new InputField(0, 6, 0, 9, attr1, 24, 80); // Data area from 6-9
        buffer.addField(field1);
        
        // Position cursor at field marker position
        buffer.setCursorPosition(0, 5);
        
        // Try to write text - should skip the field marker
        screen.putString("ABCD");
        
        // Field marker should still be there
        assertNotNull("Field marker at (0,5) should not be overwritten", buffer.getAttribute(0, 5));
        
        // Text should start after the field marker
        assertEquals("Text should start at position 6", 'A', buffer.getChar(0, 6));
        assertEquals("Text should continue at position 7", 'B', buffer.getChar(0, 7));
    }
    
    @Test
    public void testPutStringWithInsertMode() {
        // Create a screen buffer and screen
        ScreenBuffer buffer = new ScreenBuffer(24, 80);
        Screen screen = new Screen(buffer);
        
        // Create a field
        FieldAttribute attr = new FieldAttribute((byte) 0x40);
        InputField field = new InputField(0, 5, 0, 15, attr, 24, 80);
        buffer.addField(field);
        
        // Write initial text
        buffer.setCursorPosition(0, 5);
        screen.putString("ABCDEF");
        
        // Enable insert mode
        buffer.setInsertMode(true);
        
        // Position cursor in middle of text
        buffer.setCursorPosition(0, 8); // Between 'C' and 'D'
        
        // Insert text
        screen.putString("123");
        
        // Verify text was inserted, not replaced
        String fieldContent = "";
        for (int col = 5; col <= 15; col++) {
            fieldContent += buffer.getChar(0, col);
        }
        // Should be "ABC123DEF" with trailing spaces
        assertTrue("Insert mode should insert text", fieldContent.startsWith("ABC123DEF"));
    }
}
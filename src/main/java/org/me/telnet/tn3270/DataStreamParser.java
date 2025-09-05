package org.me.telnet.tn3270;

import java.io.IOException;
import org.me.io.PeekableInputStream;

public class DataStreamParser {
    private static final byte CMD_WRITE = (byte) 0xF1;
    private static final byte CMD_ERASE_WRITE = (byte) 0xF5;
    private static final byte CMD_ERASE_WRITE_ALTERNATE = (byte) 0x7E;
    private static final byte CMD_ERASE_ALL_UNPROTECTED = (byte) 0x6F;
    private static final byte CMD_READ_BUFFER = (byte) 0xF2;
    private static final byte CMD_READ_MODIFIED = (byte) 0xF6;
    private static final byte CMD_READ_MODIFIED_ALL = (byte) 0x6E;
    private static final byte CMD_WRITE_STRUCTURED_FIELD = (byte) 0xF3;
    
    private static final byte ORDER_START_FIELD = (byte) 0x1D;
    private static final byte ORDER_START_FIELD_EXTENDED = (byte) 0x29;
    private static final byte ORDER_SET_BUFFER_ADDRESS = (byte) 0x11;
    private static final byte ORDER_SET_ATTRIBUTE = (byte) 0x28;
    private static final byte ORDER_INSERT_CURSOR = (byte) 0x13;
    private static final byte ORDER_PROGRAM_TAB = (byte) 0x05;
    private static final byte ORDER_REPEAT_TO_ADDRESS = (byte) 0x3C;
    private static final byte ORDER_ERASE_UNPROTECTED_TO_ADDRESS = (byte) 0x12;
    private static final byte ORDER_MODIFY_FIELD = (byte) 0x2C;
    private static final byte ORDER_GRAPHICS_ESCAPE = (byte) 0x08;
    
    private static final byte WCC_RESET_MDT = (byte) 0x01;
    private static final byte WCC_KEYBOARD_RESTORE = (byte) 0x02;
    private static final byte WCC_SOUND_ALARM = (byte) 0x04;
    
    // Static EBCDIC to ASCII conversion table
    private static final char[] EBCDIC_TO_ASCII_TABLE = new char[256];
    
    // Initialize the conversion table once in a static block
    static {
        // Initialize with spaces
        for (int i = 0; i < 256; i++) {
            EBCDIC_TO_ASCII_TABLE[i] = ' ';
        }
        
        // Common EBCDIC to ASCII mappings
        EBCDIC_TO_ASCII_TABLE[0x00] = '\0';
        EBCDIC_TO_ASCII_TABLE[0x40] = ' ';
        EBCDIC_TO_ASCII_TABLE[0x4B] = '.';
        EBCDIC_TO_ASCII_TABLE[0x4C] = '<';
        EBCDIC_TO_ASCII_TABLE[0x4D] = '(';
        EBCDIC_TO_ASCII_TABLE[0x4E] = '+';
        EBCDIC_TO_ASCII_TABLE[0x4F] = '|';
        EBCDIC_TO_ASCII_TABLE[0x50] = '&';
        EBCDIC_TO_ASCII_TABLE[0x5A] = '!';
        EBCDIC_TO_ASCII_TABLE[0x5B] = '$';
        EBCDIC_TO_ASCII_TABLE[0x5C] = '*';
        EBCDIC_TO_ASCII_TABLE[0x5D] = ')';
        EBCDIC_TO_ASCII_TABLE[0x5E] = ';';
        EBCDIC_TO_ASCII_TABLE[0x5F] = '~';
        EBCDIC_TO_ASCII_TABLE[0x60] = '-';
        EBCDIC_TO_ASCII_TABLE[0x61] = '/';
        EBCDIC_TO_ASCII_TABLE[0x6B] = ',';
        EBCDIC_TO_ASCII_TABLE[0x6C] = '%';
        EBCDIC_TO_ASCII_TABLE[0x6D] = '_';
        EBCDIC_TO_ASCII_TABLE[0x6E] = '>';
        EBCDIC_TO_ASCII_TABLE[0x6F] = '?';
        EBCDIC_TO_ASCII_TABLE[0x79] = '`';
        EBCDIC_TO_ASCII_TABLE[0x7A] = ':';
        EBCDIC_TO_ASCII_TABLE[0x7B] = '#';
        EBCDIC_TO_ASCII_TABLE[0x7C] = '@';
        EBCDIC_TO_ASCII_TABLE[0x7D] = '\'';
        EBCDIC_TO_ASCII_TABLE[0x7E] = '=';
        EBCDIC_TO_ASCII_TABLE[0x7F] = '"';
        
        // Lowercase letters
        EBCDIC_TO_ASCII_TABLE[0x81] = 'a';
        EBCDIC_TO_ASCII_TABLE[0x82] = 'b';
        EBCDIC_TO_ASCII_TABLE[0x83] = 'c';
        EBCDIC_TO_ASCII_TABLE[0x84] = 'd';
        EBCDIC_TO_ASCII_TABLE[0x85] = 'e';
        EBCDIC_TO_ASCII_TABLE[0x86] = 'f';
        EBCDIC_TO_ASCII_TABLE[0x87] = 'g';
        EBCDIC_TO_ASCII_TABLE[0x88] = 'h';
        EBCDIC_TO_ASCII_TABLE[0x89] = 'i';
        EBCDIC_TO_ASCII_TABLE[0x91] = 'j';
        EBCDIC_TO_ASCII_TABLE[0x92] = 'k';
        EBCDIC_TO_ASCII_TABLE[0x93] = 'l';
        EBCDIC_TO_ASCII_TABLE[0x94] = 'm';
        EBCDIC_TO_ASCII_TABLE[0x95] = 'n';
        EBCDIC_TO_ASCII_TABLE[0x96] = 'o';
        EBCDIC_TO_ASCII_TABLE[0x97] = 'p';
        EBCDIC_TO_ASCII_TABLE[0x98] = 'q';
        EBCDIC_TO_ASCII_TABLE[0x99] = 'r';
        EBCDIC_TO_ASCII_TABLE[0xA2] = 's';
        EBCDIC_TO_ASCII_TABLE[0xA3] = 't';
        EBCDIC_TO_ASCII_TABLE[0xA4] = 'u';
        EBCDIC_TO_ASCII_TABLE[0xA5] = 'v';
        EBCDIC_TO_ASCII_TABLE[0xA6] = 'w';
        EBCDIC_TO_ASCII_TABLE[0xA7] = 'x';
        EBCDIC_TO_ASCII_TABLE[0xA8] = 'y';
        EBCDIC_TO_ASCII_TABLE[0xA9] = 'z';
        
        // Uppercase letters
        EBCDIC_TO_ASCII_TABLE[0xC1] = 'A';
        EBCDIC_TO_ASCII_TABLE[0xC2] = 'B';
        EBCDIC_TO_ASCII_TABLE[0xC3] = 'C';
        EBCDIC_TO_ASCII_TABLE[0xC4] = 'D';
        EBCDIC_TO_ASCII_TABLE[0xC5] = 'E';
        EBCDIC_TO_ASCII_TABLE[0xC6] = 'F';
        EBCDIC_TO_ASCII_TABLE[0xC7] = 'G';
        EBCDIC_TO_ASCII_TABLE[0xC8] = 'H';
        EBCDIC_TO_ASCII_TABLE[0xC9] = 'I';
        EBCDIC_TO_ASCII_TABLE[0xD1] = 'J';
        EBCDIC_TO_ASCII_TABLE[0xD2] = 'K';
        EBCDIC_TO_ASCII_TABLE[0xD3] = 'L';
        EBCDIC_TO_ASCII_TABLE[0xD4] = 'M';
        EBCDIC_TO_ASCII_TABLE[0xD5] = 'N';
        EBCDIC_TO_ASCII_TABLE[0xD6] = 'O';
        EBCDIC_TO_ASCII_TABLE[0xD7] = 'P';
        EBCDIC_TO_ASCII_TABLE[0xD8] = 'Q';
        EBCDIC_TO_ASCII_TABLE[0xD9] = 'R';
        EBCDIC_TO_ASCII_TABLE[0xE2] = 'S';
        EBCDIC_TO_ASCII_TABLE[0xE3] = 'T';
        EBCDIC_TO_ASCII_TABLE[0xE4] = 'U';
        EBCDIC_TO_ASCII_TABLE[0xE5] = 'V';
        EBCDIC_TO_ASCII_TABLE[0xE6] = 'W';
        EBCDIC_TO_ASCII_TABLE[0xE7] = 'X';
        EBCDIC_TO_ASCII_TABLE[0xE8] = 'Y';
        EBCDIC_TO_ASCII_TABLE[0xE9] = 'Z';
        
        // Numbers
        EBCDIC_TO_ASCII_TABLE[0xF0] = '0';
        EBCDIC_TO_ASCII_TABLE[0xF1] = '1';
        EBCDIC_TO_ASCII_TABLE[0xF2] = '2';
        EBCDIC_TO_ASCII_TABLE[0xF3] = '3';
        EBCDIC_TO_ASCII_TABLE[0xF4] = '4';
        EBCDIC_TO_ASCII_TABLE[0xF5] = '5';
        EBCDIC_TO_ASCII_TABLE[0xF6] = '6';
        EBCDIC_TO_ASCII_TABLE[0xF7] = '7';
        EBCDIC_TO_ASCII_TABLE[0xF8] = '8';
        EBCDIC_TO_ASCII_TABLE[0xF9] = '9';
    }
    
    private ScreenBuffer buffer;
    
    public DataStreamParser(ScreenBuffer buffer) {
        this.buffer = buffer;
    }
    
    public boolean isCommand(byte b) {
        return b == CMD_WRITE ||
               b == CMD_ERASE_WRITE ||
               b == CMD_ERASE_WRITE_ALTERNATE ||
               b == CMD_ERASE_ALL_UNPROTECTED ||
               b == CMD_READ_BUFFER ||
               b == CMD_READ_MODIFIED ||
               b == CMD_READ_MODIFIED_ALL ||
               b == CMD_WRITE_STRUCTURED_FIELD;
    }
    
    public boolean isWriteCommand(byte b) {
        return b == CMD_WRITE ||
               b == CMD_ERASE_WRITE ||
               b == CMD_ERASE_WRITE_ALTERNATE ||
               b == CMD_ERASE_ALL_UNPROTECTED;
    }
    
    public boolean isOrder(byte b) {
        return b == ORDER_START_FIELD ||
               b == ORDER_START_FIELD_EXTENDED ||
               b == ORDER_SET_BUFFER_ADDRESS ||
               b == ORDER_SET_ATTRIBUTE ||
               b == ORDER_INSERT_CURSOR ||
               b == ORDER_PROGRAM_TAB ||
               b == ORDER_REPEAT_TO_ADDRESS ||
               b == ORDER_ERASE_UNPROTECTED_TO_ADDRESS ||
               b == ORDER_MODIFY_FIELD ||
               b == ORDER_GRAPHICS_ESCAPE;
    }
    
    public void parse(PeekableInputStream stream) throws IOException {
        if (stream == null) {
            return;
        }
        
        byte[] peekByte = new byte[1];
        if (!stream.peek(peekByte, 0, 1)) {
            return;  // No data available
        }
        
        // Check if first byte is a valid command
        if (isCommand(peekByte[0])) {
            byte command = (byte) stream.read();
            
            if (isWriteCommand(command)) {
                if (stream.peek(peekByte, 0, 1)) {
                    byte wcc = (byte) stream.read();
                    processWriteControlCharacter(command, wcc);
                    processOrders(stream);
                }
            } else {
                processCommand(command, stream);
            }
        } else {
            // If not a command, might be raw 3270 data stream or text
            // Try to process as orders/text directly
            processOrders(stream);
        }
    }
    
    public void processWriteControlCharacter(byte command, byte wcc) {
    	String cmd = switch (command) {
    	case CMD_WRITE -> "CMD_WRITE";
    	case CMD_ERASE_WRITE -> "CMD_ERASE_WRITE";
    	case CMD_ERASE_WRITE_ALTERNATE -> "CMD_ERASE_WRITE_ALTERNATE";
    	case CMD_ERASE_ALL_UNPROTECTED -> "CMD_ERASE_ALL_UNPROTECTED";
    	default -> "UNKNOWN";
    	};
    	
    	System.out.println(command + " " + cmd);
    	System.out.println(wcc + " wcc"); 	
        if ((wcc & WCC_RESET_MDT) != 0) {
            resetModifiedDataTags();//null, 0);
        }
        
        if ((wcc & WCC_KEYBOARD_RESTORE) != 0) {
            buffer.setKeyboardLocked(false);
        } else {
            buffer.setKeyboardLocked(true);
        }
        
        if ((wcc & WCC_SOUND_ALARM) != 0) {
            System.out.print('\007');
        }
        
        if (command == CMD_ERASE_WRITE || command == CMD_ERASE_WRITE_ALTERNATE) {
            buffer.clear();
        } else if (command == CMD_ERASE_ALL_UNPROTECTED) {
            eraseAllUnprotected();
        }
    }
    
    private void processCommand(byte command, PeekableInputStream stream) throws IOException {
        switch (command) {
            case CMD_READ_BUFFER:
            case CMD_READ_MODIFIED:
            case CMD_READ_MODIFIED_ALL:
                break;
            case CMD_WRITE_STRUCTURED_FIELD:
                processStructuredField(stream);
                break;
        }
    }
    
    private void processStructuredField(PeekableInputStream stream) throws IOException {
        // Read structured field data from stream as needed
    }
    
    private void eraseAllUnprotected() {
        for (InputField field : buffer.getFields()) {
            if (field.canInput()) {
                field.clearData();
            }
        }
    }
    
    public void processOrders(PeekableInputStream stream) throws IOException {
        byte[] peekByte = new byte[1];
        while (stream.peek(peekByte, 0, 1)) {
            if (isOrder(peekByte[0])) {
                processOrder(stream);
            } else {
                processCharacter(stream);
            }
        }
    }
    
    public void resetModifiedDataTags() {
        buffer.resetModifiedFlags();
    }
    
    public void processOrder(PeekableInputStream stream) throws IOException {
        byte order = (byte) stream.read();
        
        switch (order) {
            case ORDER_SET_BUFFER_ADDRESS:
                System.out.println(order + " ORD_SBA");
                processSetBufferAddress(stream);
                break;
            case ORDER_START_FIELD:
                System.out.println(order + " ORD_SF");
                processStartField(stream);
                break;
            case ORDER_START_FIELD_EXTENDED:
                System.out.println(order + " ORD_SFE");
                processStartFieldExtended(stream);
                break;
            case ORDER_SET_ATTRIBUTE:
                System.out.println(order + " ORD_SA");
                processSetAttribute(stream);
                break;
            case ORDER_INSERT_CURSOR:
                System.out.println(order + " ORD_IC");
                processInsertCursor(stream);
                break;
            case ORDER_PROGRAM_TAB:
                System.out.println(order + " ORD_PT");
                processProgramTab(stream);
                break;
            case ORDER_REPEAT_TO_ADDRESS:
                System.out.println(order + " ORD_RA");
                processRepeatToAddress(stream);
                break;
            case ORDER_ERASE_UNPROTECTED_TO_ADDRESS:
                System.out.println(order + " ORD_EUA");
                processEraseUnprotectedToAddress(stream);
                break;
            case ORDER_MODIFY_FIELD:
                System.out.println(order + " ORD_MF");
                processModifyField(stream);
                break;
            case ORDER_GRAPHICS_ESCAPE:
                System.out.println(order + " ORD_GE");
                processGraphicsEscape(stream);
                break;
        }
    }
    
    // Stream-based order processing methods
    public void processSetBufferAddress(PeekableInputStream stream) throws IOException {
        byte[] addressBytes = new byte[2];
        if (stream.read(addressBytes, 0, 2) == 2) {
            int address = decodeAddress(addressBytes[0], addressBytes[1]);
            buffer.setCursorAddress(address);
        }
    }
    
    public void processStartField(PeekableInputStream stream) throws IOException {
        byte attributeByte = (byte) stream.read();
        FieldAttribute attribute = new FieldAttribute(attributeByte);
        
        int fieldStart = buffer.getCursorAddress();
        buffer.setAttribute(fieldStart, attribute);
        
        int nextFieldStart = findNextFieldStart(fieldStart);
        int fieldEnd = (nextFieldStart - 1 + buffer.getRows() * buffer.getCols()) % 
                      (buffer.getRows() * buffer.getCols());
        
        int startRow = (fieldStart / buffer.getCols()) + 1;
        int startCol = (fieldStart % buffer.getCols()) + 1;
        int endRow = (fieldEnd / buffer.getCols()) + 1;
        int endCol = (fieldEnd % buffer.getCols()) + 1;
        
        InputField field = new InputField(startRow, startCol, endRow, endCol, attribute);
        buffer.addField(field);
        
        buffer.moveCursorRight();
    }
    
    public void processStartFieldExtended(PeekableInputStream stream) throws IOException {
        int count = stream.read() & 0xFF;
        byte attributeByte = 0;
        
        for (int i = 0; i < count; i++) {
            byte type = (byte) stream.read();
            byte value = (byte) stream.read();
            
            if (type == (byte) 0xC0) {
                attributeByte = value;
            }
        }
        
        FieldAttribute attribute = new FieldAttribute(attributeByte);
        int fieldStart = buffer.getCursorAddress();
        buffer.setAttribute(fieldStart, attribute);
        
        int nextFieldStart = findNextFieldStart(fieldStart);
        int fieldEnd = (nextFieldStart - 1 + buffer.getRows() * buffer.getCols()) % 
                      (buffer.getRows() * buffer.getCols());
        
        int startRow = (fieldStart / buffer.getCols()) + 1;
        int startCol = (fieldStart % buffer.getCols()) + 1;
        int endRow = (fieldEnd / buffer.getCols()) + 1;
        int endCol = (fieldEnd % buffer.getCols()) + 1;
        
        InputField field = new InputField(startRow, startCol, endRow, endCol, attribute);
        buffer.addField(field);
        
        buffer.moveCursorRight();
    }
    
    public void processSetAttribute(PeekableInputStream stream) throws IOException {
        byte type = (byte) stream.read();
        byte value = (byte) stream.read();
        
        // Get current cursor position
        int currentAddress = buffer.getCursorAddress();
        int row = (currentAddress / buffer.getCols()) + 1;
        int col = (currentAddress % buffer.getCols()) + 1;
        
        // Get the field at current position (if any)
        InputField field = buffer.getFieldAt(row, col);
        
        // Process the attribute type according to 3270 protocol
        switch (type) {
            case (byte) 0x00: // Reset all attributes
                // Reset to default attribute
                FieldAttribute defaultAttr = new FieldAttribute();
                if (field != null) {
                    field.attribute(defaultAttr);
                } else {
                    buffer.setAttribute(currentAddress, defaultAttr);
                }
                break;
                
            case (byte) 0x41: // Extended highlighting
                // Process highlighting attribute (blink, reverse video, underline)
                FieldHighlighting highlighting = null;
                switch (value) {
                    case (byte) 0x00: // Default
                        highlighting = FieldHighlighting.NORMAL;
                        break;
                    case (byte) 0xF1: // Blink
                        highlighting = FieldHighlighting.BLINK;
                        break;
                    case (byte) 0xF2: // Reverse video
                        highlighting = FieldHighlighting.REVERSE;
                        break;
                    case (byte) 0xF4: // Underscore
                        highlighting = FieldHighlighting.UNDERSCORE;
                        break;
                }
                if (highlighting != null) {
                    if (field != null) {
                        field.attribute().highlighting(highlighting);
                    } else {
                        FieldAttribute attr = buffer.getAttribute(row, col);
                        if (attr != null) {
                            attr.highlighting(highlighting);
                        }
                    }
                }
                break;
                
            case (byte) 0x42: // Foreground color
                // Process color attribute
                FieldColor color = null;
                switch (value) {
                    case (byte) 0x00: // Default color
                        color = FieldColor.DEFAULT;
                        break;
                    case (byte) 0xF1: // Blue
                        color = FieldColor.BLUE;
                        break;
                    case (byte) 0xF2: // Red
                        color = FieldColor.RED;
                        break;
                    case (byte) 0xF3: // Pink/Magenta
                        color = FieldColor.PINK;
                        break;
                    case (byte) 0xF4: // Green
                        color = FieldColor.GREEN;
                        break;
                    case (byte) 0xF5: // Turquoise/Cyan
                        color = FieldColor.TURQUOISE;
                        break;
                    case (byte) 0xF6: // Yellow
                        color = FieldColor.YELLOW;
                        break;
                    case (byte) 0xF7: // White/Neutral
                        color = FieldColor.WHITE;
                        break;
                }
                if (color != null) {
                    if (field != null) {
                        field.attribute().color(color);
                    } else {
                        FieldAttribute attr = buffer.getAttribute(row, col);
                        if (attr != null) {
                            attr.color(color);
                        }
                    }
                }
                break;
                
            case (byte) 0x43: // Character set
                // Character set selection - typically for APL, text, etc.
                // For now, just log it
                System.out.println("SA: Character set value: 0x" + Integer.toHexString(value & 0xFF));
                break;
                
            case (byte) 0x44: // Background color
                // Background color - implementation depends on terminal capabilities
                System.out.println("SA: Background color value: 0x" + Integer.toHexString(value & 0xFF));
                break;
                
            case (byte) 0x45: // Transparency
                // Field transparency - implementation depends on terminal capabilities
                System.out.println("SA: Transparency value: 0x" + Integer.toHexString(value & 0xFF));
                break;
                
            default:
                // Unknown attribute type
                System.out.println("SA: Unknown attribute type: 0x" + Integer.toHexString(type & 0xFF) 
                    + " value: 0x" + Integer.toHexString(value & 0xFF));
                break;
        }
    }
    
    public void processInsertCursor(PeekableInputStream stream) throws IOException {
        // Read the 2-byte buffer address where cursor should be positioned
        byte[] addressBytes = new byte[2];
        if (stream.read(addressBytes, 0, 2) == 2) {
            int address = decodeAddress(addressBytes[0], addressBytes[1]);
            
            // Update the cursor address in the buffer
            buffer.setCursorAddress(address);
            
            // Also update cursor row and column based on the address
            int row = (address / buffer.getCols()) + 1;
            int col = (address % buffer.getCols()) + 1;
            buffer.setCursorPosition(row, col);
        }
    }
    
    public void processProgramTab(PeekableInputStream stream) throws IOException {
        // Tab to next field
        buffer.moveCursorToNextUnprotectedField();
    }
    
    public void processRepeatToAddress(PeekableInputStream stream) throws IOException {
        byte[] addressBytes = new byte[2];
        if (stream.read(addressBytes, 0, 2) == 2) {
            int endAddress = decodeAddress(addressBytes[0], addressBytes[1]);
            byte repeatChar = (byte) stream.read();
            
            int currentAddress = buffer.getCursorAddress();
            while (currentAddress != endAddress) {
                int row = (currentAddress / buffer.getCols()) + 1;
                int col = (currentAddress % buffer.getCols()) + 1;
                buffer.setChar(row, col, (char) (repeatChar & 0xFF));
                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
            }
            buffer.setCursorAddress(endAddress);
        }
    }
    
    public void processEraseUnprotectedToAddress(PeekableInputStream stream) throws IOException {
        byte[] addressBytes = new byte[2];
        if (stream.read(addressBytes, 0, 2) == 2) {
            int endAddress = decodeAddress(addressBytes[0], addressBytes[1]);
            
            int currentAddress = buffer.getCursorAddress();
            while (currentAddress != endAddress) {
                int row = (currentAddress / buffer.getCols()) + 1;
                int col = (currentAddress % buffer.getCols()) + 1;
                
                InputField field = buffer.getFieldAt(row, col);
                if (field != null && field.canInput()) {
                    buffer.setChar(row, col, ' ');
                }
                
                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
            }
            buffer.setCursorAddress(endAddress);
        }
    }
    
    public void processModifyField(PeekableInputStream stream) throws IOException {
        // Read the count of attribute type/value pairs
        int count = stream.read() & 0xFF;
        
        // Get the field at current cursor position
        int currentAddress = buffer.getCursorAddress();
        int row = (currentAddress / buffer.getCols()) + 1;
        int col = (currentAddress % buffer.getCols()) + 1;
        
        // Find the field that starts at or contains the current position
        InputField field = null;
        for (InputField f : buffer.getFields()) {
            if (f.getFieldPosition() == currentAddress) {
                // Field starts at current position
                field = f;
                break;
            } else if (f.containsPosition(row, col)) {
                // Current position is within this field
                field = f;
                break;
            }
        }
        
        if (field == null) {
            // No field at current position, skip the attribute pairs
            for (int i = 0; i < count; i++) {
                stream.read(); // type
                stream.read(); // value
            }
            System.out.println("MF: No field found at position " + currentAddress);
            return;
        }
        
        // Process each attribute type/value pair
        for (int i = 0; i < count; i++) {
            byte type = (byte) stream.read();
            byte value = (byte) stream.read();
            
            // Process different modification types according to 3270 spec
            switch (type) {
                case (byte) 0xC0: // Basic field attribute
                    // Replace the entire field attribute
                    field.attribute(new FieldAttribute(value));
                    System.out.println("MF: Set field attribute to 0x" + Integer.toHexString(value & 0xFF));
                    break;
                    
                case (byte) 0xC1: // Field highlighting
                    // Set field highlighting (blink, reverse video, underline)
                    FieldHighlighting highlighting = null;
                    switch (value) {
                        case (byte) 0x00: highlighting = FieldHighlighting.NORMAL; break;
                        case (byte) 0xF1: highlighting = FieldHighlighting.BLINK; break;
                        case (byte) 0xF2: highlighting = FieldHighlighting.REVERSE; break;
                        case (byte) 0xF4: highlighting = FieldHighlighting.UNDERSCORE; break;
                        default:
                            System.out.println("MF: Unknown highlighting value: 0x" + Integer.toHexString(value & 0xFF));
                    }
                    if (highlighting != null) {
                        field.attribute().highlighting(highlighting);
                    }
                    break;
                    
                case (byte) 0xC2: // Field color
                    // Set field color
                    FieldColor color = null;
                    switch (value) {
                        case (byte) 0x00: color = FieldColor.DEFAULT; break;
                        case (byte) 0xF1: color = FieldColor.BLUE; break;
                        case (byte) 0xF2: color = FieldColor.RED; break;
                        case (byte) 0xF3: color = FieldColor.PINK; break;
                        case (byte) 0xF4: color = FieldColor.GREEN; break;
                        case (byte) 0xF5: color = FieldColor.TURQUOISE; break;
                        case (byte) 0xF6: color = FieldColor.YELLOW; break;
                        case (byte) 0xF7: color = FieldColor.WHITE; break;
                        default:
                            System.out.println("MF: Unknown color value: 0x" + Integer.toHexString(value & 0xFF));
                    }
                    if (color != null) {
                        field.attribute().color(color);
                    }
                    break;
                    
                case (byte) 0xC3: // Field character set
                    // Set character set for the field
                    System.out.println("MF: Character set value: 0x" + Integer.toHexString(value & 0xFF));
                    // Implementation would depend on terminal support
                    break;
                    
                case (byte) 0xC4: // Field outlining  
                    // Set field outlining (box around field)
                    System.out.println("MF: Outlining value: 0x" + Integer.toHexString(value & 0xFF));
                    // Common values: 0x00=no outline, 0x01=underline, 0x02=right line, 0x04=overline, 0x08=left line
                    break;
                    
                case (byte) 0xC5: // Field transparency
                    // Set field transparency
                    System.out.println("MF: Transparency value: 0x" + Integer.toHexString(value & 0xFF));
                    // 0x00=opaque, 0xF0=transparent
                    break;
                    
                case (byte) 0xC6: // Field validation
                    // Set field validation type (mandatory fill, trigger, etc.)
                    System.out.println("MF: Validation value: 0x" + Integer.toHexString(value & 0xFF));
                    // 0x00=normal, 0x01=mandatory fill, 0x02=mandatory enter, 0x03=trigger
                    break;
                    
                case (byte) 0xC7: // Field modification
                    // Modify MDT (Modified Data Tag) flag
                    if (value == 0x00) {
                        field.modified(false);
                        System.out.println("MF: Reset MDT flag");
                    } else {
                        field.modified(true);
                        System.out.println("MF: Set MDT flag");
                    }
                    break;
                    
                case (byte) 0xC8: // Field intensity
                    // Set field intensity
                    FieldIntensity intensity = null;
                    switch (value) {
                        case (byte) 0x00: intensity = FieldIntensity.NORMAL; break;
                        case (byte) 0xF0: intensity = FieldIntensity.NORMAL; break;
                        case (byte) 0xF1: intensity = FieldIntensity.HIGH; break;
                        case (byte) 0xF2: intensity = FieldIntensity.ZERO; break;
                        default:
                            System.out.println("MF: Unknown intensity value: 0x" + Integer.toHexString(value & 0xFF));
                    }
                    if (intensity != null) {
                        field.attribute().intensity(intensity);
                    }
                    break;
                    
                default:
                    // Unknown attribute type
                    System.out.println("MF: Unknown attribute type: 0x" + Integer.toHexString(type & 0xFF) 
                        + " value: 0x" + Integer.toHexString(value & 0xFF));
                    break;
            }
        }
        
        // Mark field as having been modified if any attributes changed
        field.modified(true);
        System.out.println("MF: Modified field at address " + currentAddress);
    }
    
    public void processGraphicsEscape(PeekableInputStream stream) throws IOException {
        // Read the graphics character byte that follows the GE order
        int graphicsByte = stream.read();
        if (graphicsByte == -1) {
            return; // End of stream
        }
        
        // The graphics character is typically not converted through EBCDIC
        // as it represents a special graphics symbol (box drawing, etc.)
        // In 3270, graphics characters are in the range 0x40-0xFE
        char graphicsChar = (char) (graphicsByte & 0xFF);
        
        // Get current cursor position
        int currentAddress = buffer.getCursorAddress();
        int row = (currentAddress / buffer.getCols()) + 1;
        int col = (currentAddress % buffer.getCols()) + 1;
        
        // Place the graphics character at the current cursor position
        buffer.setChar(row, col, graphicsChar);
        
        // Move cursor to the next position after placing the character
        buffer.moveCursorRight();
        
        // Debug output if needed
        System.out.println("GE: Placed graphics char 0x" + Integer.toHexString(graphicsByte & 0xFF) 
            + " at row " + row + ", col " + col);
    }
    
    public void processCharacter(PeekableInputStream stream) throws IOException {
        int ch = stream.read();
        if (ch != -1) {
            char displayChar = ebcdicToAscii((byte) ch);
            int row = (buffer.getCursorAddress() / buffer.getCols()) + 1;
            int col = (buffer.getCursorAddress() % buffer.getCols()) + 1;
            
            buffer.setChar(row, col, displayChar);
            buffer.moveCursorRight();
        }
    }
    
    private int findNextFieldStart(int currentPosition) {
        int totalPositions = buffer.getRows() * buffer.getCols();
        int nextPosition = (currentPosition + 1) % totalPositions;
        
        while (nextPosition != currentPosition) {
            if (buffer.getAttribute((nextPosition / buffer.getCols()) + 1,
                                   (nextPosition % buffer.getCols()) + 1) != null) {
                return nextPosition;
            }
            nextPosition = (nextPosition + 1) % totalPositions;
        }
        
        return currentPosition;
    }
    
    public int decodeAddress(byte high, byte low) {
        int h = high & 0xFF;
        int l = low & 0xFF;
        
        int address = ((h & 0x3F) << 6) | (l & 0x3F);
        
        System.out.println("high " + h);
        System.out.println(low + " low");
        System.out.println(address + " final pos");
        
        return address;
    }
    
    public int[] getRowColumnFromAddress(int position) {
        int row = (position / buffer.getCols()) + 1;
        int col = (position % buffer.getCols()) + 1;
        return new int[] {row, col};
    }
    
    private char ebcdicToAscii(int ebcdic) {
        // Simply lookup the character in the static conversion table
        return EBCDIC_TO_ASCII_TABLE[ebcdic & 0xFF];
    }
}
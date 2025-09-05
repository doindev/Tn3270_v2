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
    
    // Keep original parse method for compatibility
//    public void parse(byte[] data) {
//        if (data == null || data.length == 0) {
//            return;
//        }
//        
//        // Debug output - uncomment if needed
//        // System.out.println("DataStreamParser: Received " + data.length + " bytes");
//        // for (int i = 0; i < Math.min(data.length, 20); i++) {
//        //     System.out.printf("%02X ", data[i] & 0xFF);
//        // }
//        // System.out.println();
//        
//        int pos = 0;
//        
//        // Check if first byte is a valid command
//        if (data.length >= 2 && isCommand(data[pos])) {
//            byte command = data[pos++];
//            
//            if (isWriteCommand(command)) {
//                if (pos < data.length) {
//                    byte wcc = data[pos++];
//                    processWriteControlCharacter(command, wcc);
//                    processOrders(data, pos);
//                }
//            } else {
//                processCommand(command, data, pos);
//            }
//        } else {
//            // If not a command, might be raw 3270 data stream or text
//            // Try to process as orders/text directly
//            processOrders(data, pos);
//        }
//    }
    
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
    
//    private void processCommand(byte command, byte[] data, int pos) {
//        switch (command) {
//            case CMD_READ_BUFFER:
//            case CMD_READ_MODIFIED:
//            case CMD_READ_MODIFIED_ALL:
//                break;
//            case CMD_WRITE_STRUCTURED_FIELD:
//                processStructuredField(data, pos);
//                break;
//        }
//    }
    
    private void processStructuredField(PeekableInputStream stream) throws IOException {
        // Read structured field data from stream as needed
    }
    
//    private void processStructuredField(byte[] data, int pos) {
//    }
    
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
    
//    public void processOrders(byte[] data, int pos) {
//        while (pos < data.length) {
//            byte b = data[pos];
//            if (isOrder(b)) {
//                pos = processOrder(data, pos);
//            } else {
//                pos = processCharacter(data, pos);
//            }
//        }
//    }
    
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
    
//    public int processOrder(byte[] data, int pos) {
//        if (pos >= data.length) {
//            return pos;
//        }
//        
//        byte order = data[pos++];
//        
//        switch (order) {
//            case ORDER_SET_BUFFER_ADDRESS:
//            	System.out.println(order + " ORD_SBA");
//                return processSetBufferAddress(data, pos);
//            case ORDER_START_FIELD:
//            	System.out.println(order + " ORD_SF");
//                return processStartField(data, pos);
//            case ORDER_START_FIELD_EXTENDED:
//            	System.out.println(order + " ORD_SFE");
//                return processStartFieldExtended(data, pos);
//            case ORDER_SET_ATTRIBUTE:
//            	System.out.println(order + " ORD_SA");
//                return processSetAttribute(data, pos);
//            case ORDER_INSERT_CURSOR:
//            	System.out.println(order + " ORD_IC");
//                return processInsertCursor(data, pos);
//            case ORDER_PROGRAM_TAB:
//            	System.out.println(order + " ORD_PT");
//                return processProgramTab(data, pos);
//            case ORDER_REPEAT_TO_ADDRESS:
//            	System.out.println(order + " ORD_RA");
//                return processRepeatToAddress(data, pos);
//            case ORDER_ERASE_UNPROTECTED_TO_ADDRESS:
//            	System.out.println(order + " ORD_EUA");
//                return processEraseUnprotectedToAddress(data, pos);
//            case ORDER_MODIFY_FIELD:
//            	System.out.println(order + " ORD_MF");
//                return processModifyField(data, pos);
//            case ORDER_GRAPHICS_ESCAPE:
//            	System.out.println(order + " ORD_GE");
//                return processGraphicsEscape(data, pos);
//            default:
//                return pos;
//        }
//    }
    
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
        // Process attribute setting
    }
    
    public void processInsertCursor(PeekableInputStream stream) throws IOException {
        // Insert cursor at current position
        // Note: setCursorVisible not implemented in ScreenBuffer yet
        // This would typically show the cursor at the current position
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
        int count = stream.read() & 0xFF;
        for (int i = 0; i < count; i++) {
            byte type = (byte) stream.read();
            byte value = (byte) stream.read();
            // Process field modification
        }
    }
    
    public void processGraphicsEscape(PeekableInputStream stream) throws IOException {
        byte graphicsChar = (byte) stream.read();
        // Process graphics escape
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
    
//    public int processSetBufferAddress(byte[] data, int pos) {
//        if (pos + 1 < data.length) {
//            int address = decodeAddress(data[pos], data[pos + 1]);
//            buffer.setCursorAddress(address);
//            return pos + 2;
//        }
//        return pos;
//    }
    
//    public int processStartField(byte[] data, int pos) {
//        if (pos < data.length) {
//            byte attributeByte = data[pos++];
//            FieldAttribute attribute = new FieldAttribute(attributeByte);
//            
//            int fieldStart = buffer.getCursorAddress();
//            buffer.setAttribute(fieldStart, attribute);
//            
//            int nextFieldStart = findNextFieldStart(fieldStart);
//            int fieldEnd = (nextFieldStart - 1 + buffer.getRows() * buffer.getCols()) % 
//                          (buffer.getRows() * buffer.getCols());
//            
//            int startRow = (fieldStart / buffer.getCols()) + 1;
//            int startCol = (fieldStart % buffer.getCols()) + 1;
//            int endRow = (fieldEnd / buffer.getCols()) + 1;
//            int endCol = (fieldEnd % buffer.getCols()) + 1;
//            
//            InputField field = new InputField(startRow, startCol, endRow, endCol, attribute);
//            buffer.addField(field);
//            
//            buffer.moveCursorRight();
//        }
//        return pos;
//    }
    
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
    
    public int processStartFieldExtended(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            int count = data[pos++] & 0xFF;
            byte attributeByte = 0;
            
            for (int i = 0; i < count && pos + 1 < data.length; i++) {
                byte type = data[pos++];
                byte value = data[pos++];
                
                if (type == 0xC0) {
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
        return pos;
    }
    
//    public int processSetAttribute(byte[] data, int pos) {
//        if (pos + 1 < data.length) {
//            byte type = data[pos++];
//            byte value = data[pos++];
//        }
//        return pos;
//    }
    
//    public int processInsertCursor(byte[] data, int pos) {
//        if (pos + 1 < data.length) {
//            int address = decodeAddress(data[pos], data[pos + 1]);
//            buffer.setCursorAddress(address);
//            return pos + 2;
//        }
//        return pos;
//    }
    
//    public int processProgramTab(byte[] data, int pos) {
//        buffer.moveCursorToNextUnprotectedField();
//        return pos;
//    }
    
//    public int processRepeatToAddress(byte[] data, int pos) {
//        if (pos + 2 < data.length) {
//            int address = decodeAddress(data[pos], data[pos + 1]);
//            char ch = (char) (data[pos + 2] & 0xFF);
//            
//            int currentAddress = buffer.getCursorAddress();
//            while (currentAddress != address) {
//                buffer.setChar(currentAddress, ch);
//                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
//            }
//            
//            return pos + 3;
//        }
//        return pos;
//    }
    
//    public int processEraseUnprotectedToAddress(byte[] data, int pos) {
//        if (pos + 1 < data.length) {
//            int address = decodeAddress(data[pos], data[pos + 1]);
//            
//            int currentAddress = buffer.getCursorAddress();
//            while (currentAddress != address) {
//                InputField field = buffer.getFieldAt(currentAddress);
//                if (field == null || field.canInput()) {
//                    buffer.setChar(currentAddress, ' ');
//                }
//                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
//            }
//            
//            return pos + 2;
//        }
//        return pos;
//    }
    
//    public int processModifyField(byte[] data, int pos) {
//        if (pos + 1 < data.length) {
//            int count = data[pos++] & 0xFF;
//            
//            for (int i = 0; i < count && pos + 1 < data.length; i++) {
//                byte type = data[pos++];
//                byte value = data[pos++];
//            }
//        }
//        return pos;
//    }
    
//    public int processGraphicsEscape(byte[] data, int pos) {
//        if (pos < data.length) {
//            char ch = (char) (data[pos++] & 0xFF);
//            buffer.setChar(buffer.getCursorAddress(), ch);
//            buffer.moveCursorRight();
//        }
//        return pos;
//    }
    
//    public int processCharacter(byte[] data, int pos) {
//        if (pos < data.length) {
//            int ebcdic = data[pos++] & 0xFF;
//            char ch = ebcdicToAscii(ebcdic);
//            
//            System.out.println(ebcdic + " " + ch);
//            
//            // Debug output - uncomment if needed
//            // System.out.printf("Processing character: EBCDIC=0x%02X ASCII='%c' at position %d%n", 
//            //                 ebcdic, ch, buffer.getCursorAddress());
//            
//            buffer.setChar(buffer.getCursorAddress(), ch);
//            buffer.moveCursorRight();
//        }
//        return pos;
//    }
    
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
        // Full EBCDIC to ASCII conversion table
        char[] conversionTable = new char[256];
        
        // Initialize with spaces
        for (int i = 0; i < 256; i++) {
            conversionTable[i] = ' ';
        }
        
        // Common EBCDIC to ASCII mappings
        conversionTable[0x00] = '\0';
        conversionTable[0x40] = ' ';
        conversionTable[0x4B] = '.';
        conversionTable[0x4C] = '<';
        conversionTable[0x4D] = '(';
        conversionTable[0x4E] = '+';
        conversionTable[0x4F] = '|';
        conversionTable[0x50] = '&';
        conversionTable[0x5A] = '!';
        conversionTable[0x5B] = '$';
        conversionTable[0x5C] = '*';
        conversionTable[0x5D] = ')';
        conversionTable[0x5E] = ';';
        conversionTable[0x5F] = '~';
        conversionTable[0x60] = '-';
        conversionTable[0x61] = '/';
        conversionTable[0x6B] = ',';
        conversionTable[0x6C] = '%';
        conversionTable[0x6D] = '_';
        conversionTable[0x6E] = '>';
        conversionTable[0x6F] = '?';
        conversionTable[0x79] = '`';
        conversionTable[0x7A] = ':';
        conversionTable[0x7B] = '#';
        conversionTable[0x7C] = '@';
        conversionTable[0x7D] = '\'';
        conversionTable[0x7E] = '=';
        conversionTable[0x7F] = '"';
        
        // Letters
        conversionTable[0x81] = 'a';
        conversionTable[0x82] = 'b';
        conversionTable[0x83] = 'c';
        conversionTable[0x84] = 'd';
        conversionTable[0x85] = 'e';
        conversionTable[0x86] = 'f';
        conversionTable[0x87] = 'g';
        conversionTable[0x88] = 'h';
        conversionTable[0x89] = 'i';
        conversionTable[0x91] = 'j';
        conversionTable[0x92] = 'k';
        conversionTable[0x93] = 'l';
        conversionTable[0x94] = 'm';
        conversionTable[0x95] = 'n';
        conversionTable[0x96] = 'o';
        conversionTable[0x97] = 'p';
        conversionTable[0x98] = 'q';
        conversionTable[0x99] = 'r';
        conversionTable[0xA2] = 's';
        conversionTable[0xA3] = 't';
        conversionTable[0xA4] = 'u';
        conversionTable[0xA5] = 'v';
        conversionTable[0xA6] = 'w';
        conversionTable[0xA7] = 'x';
        conversionTable[0xA8] = 'y';
        conversionTable[0xA9] = 'z';
        
        conversionTable[0xC1] = 'A';
        conversionTable[0xC2] = 'B';
        conversionTable[0xC3] = 'C';
        conversionTable[0xC4] = 'D';
        conversionTable[0xC5] = 'E';
        conversionTable[0xC6] = 'F';
        conversionTable[0xC7] = 'G';
        conversionTable[0xC8] = 'H';
        conversionTable[0xC9] = 'I';
        conversionTable[0xD1] = 'J';
        conversionTable[0xD2] = 'K';
        conversionTable[0xD3] = 'L';
        conversionTable[0xD4] = 'M';
        conversionTable[0xD5] = 'N';
        conversionTable[0xD6] = 'O';
        conversionTable[0xD7] = 'P';
        conversionTable[0xD8] = 'Q';
        conversionTable[0xD9] = 'R';
        conversionTable[0xE2] = 'S';
        conversionTable[0xE3] = 'T';
        conversionTable[0xE4] = 'U';
        conversionTable[0xE5] = 'V';
        conversionTable[0xE6] = 'W';
        conversionTable[0xE7] = 'X';
        conversionTable[0xE8] = 'Y';
        conversionTable[0xE9] = 'Z';
        
        // Numbers
        conversionTable[0xF0] = '0';
        conversionTable[0xF1] = '1';
        conversionTable[0xF2] = '2';
        conversionTable[0xF3] = '3';
        conversionTable[0xF4] = '4';
        conversionTable[0xF5] = '5';
        conversionTable[0xF6] = '6';
        conversionTable[0xF7] = '7';
        conversionTable[0xF8] = '8';
        conversionTable[0xF9] = '9';
        
        return conversionTable[ebcdic & 0xFF];
    }
}
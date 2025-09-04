package org.metoo.telnet.tn3270;

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
    
    public void parse(byte[] data) {
        if (data == null || data.length < 2) {
            return;
        }
        
        int pos = 0;
        byte command = data[pos++];
        
        if (isWriteCommand(command)) {
            byte wcc = data[pos++];
            processWriteControlCharacter(command, wcc);
            processOrders(data, pos);
        } else if (isCommand(command)) {
            processCommand(command, data, pos);
        }
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
    
    public void processWriteControlCharacter(byte command, byte wcc) {
        if ((wcc & WCC_RESET_MDT) != 0) {
            resetModifiedDataTags(null, 0);
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
    
    private void processCommand(byte command, byte[] data, int pos) {
        switch (command) {
            case CMD_READ_BUFFER:
            case CMD_READ_MODIFIED:
            case CMD_READ_MODIFIED_ALL:
                break;
            case CMD_WRITE_STRUCTURED_FIELD:
                processStructuredField(data, pos);
                break;
        }
    }
    
    private void processStructuredField(byte[] data, int pos) {
    }
    
    private void eraseAllUnprotected() {
        for (InputField field : buffer.getFields()) {
            if (field.canInput()) {
                field.clearData();
            }
        }
    }
    
    public void processOrders(byte[] data, int pos) {
        while (pos < data.length) {
            byte b = data[pos];
            if (isOrder(b)) {
                pos = processOrder(data, pos);
            } else {
                pos = processCharacter(data, pos);
            }
        }
    }
    
    public void resetModifiedDataTags(byte[] data, int pos) {
        buffer.resetModifiedFlags();
    }
    
    public int processOrder(byte[] data, int pos) {
        if (pos >= data.length) {
            return pos;
        }
        
        byte order = data[pos++];
        
        switch (order) {
            case ORDER_SET_BUFFER_ADDRESS:
                return processSetBufferAddress(data, pos);
            case ORDER_START_FIELD:
                return processStartField(data, pos);
            case ORDER_START_FIELD_EXTENDED:
                return processStartFieldExtended(data, pos);
            case ORDER_SET_ATTRIBUTE:
                return processSetAttribute(data, pos);
            case ORDER_INSERT_CURSOR:
                return processInsertCursor(data, pos);
            case ORDER_PROGRAM_TAB:
                return processProgramTab(data, pos);
            case ORDER_REPEAT_TO_ADDRESS:
                return processRepeatToAddress(data, pos);
            case ORDER_ERASE_UNPROTECTED_TO_ADDRESS:
                return processEraseUnprotectedToAddress(data, pos);
            case ORDER_MODIFY_FIELD:
                return processModifyField(data, pos);
            case ORDER_GRAPHICS_ESCAPE:
                return processGraphicsEscape(data, pos);
            default:
                return pos;
        }
    }
    
    public int processSetBufferAddress(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            int address = decodeAddress(data[pos], data[pos + 1]);
            buffer.setCursorAddress(address);
            return pos + 2;
        }
        return pos;
    }
    
    public int processStartField(byte[] data, int pos) {
        if (pos < data.length) {
            byte attributeByte = data[pos++];
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
    
    public int processSetAttribute(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            byte type = data[pos++];
            byte value = data[pos++];
        }
        return pos;
    }
    
    public int processInsertCursor(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            int address = decodeAddress(data[pos], data[pos + 1]);
            buffer.setCursorAddress(address);
            return pos + 2;
        }
        return pos;
    }
    
    public int processProgramTab(byte[] data, int pos) {
        buffer.moveCursorToNextUnprotectedField();
        return pos;
    }
    
    public int processRepeatToAddress(byte[] data, int pos) {
        if (pos + 2 < data.length) {
            int address = decodeAddress(data[pos], data[pos + 1]);
            char ch = (char) (data[pos + 2] & 0xFF);
            
            int currentAddress = buffer.getCursorAddress();
            while (currentAddress != address) {
                buffer.setChar(currentAddress, ch);
                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
            }
            
            return pos + 3;
        }
        return pos;
    }
    
    public int processEraseUnprotectedToAddress(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            int address = decodeAddress(data[pos], data[pos + 1]);
            
            int currentAddress = buffer.getCursorAddress();
            while (currentAddress != address) {
                InputField field = buffer.getFieldAt(currentAddress);
                if (field == null || field.canInput()) {
                    buffer.setChar(currentAddress, ' ');
                }
                currentAddress = (currentAddress + 1) % (buffer.getRows() * buffer.getCols());
            }
            
            return pos + 2;
        }
        return pos;
    }
    
    public int processModifyField(byte[] data, int pos) {
        if (pos + 1 < data.length) {
            int count = data[pos++] & 0xFF;
            
            for (int i = 0; i < count && pos + 1 < data.length; i++) {
                byte type = data[pos++];
                byte value = data[pos++];
            }
        }
        return pos;
    }
    
    public int processGraphicsEscape(byte[] data, int pos) {
        if (pos < data.length) {
            char ch = (char) (data[pos++] & 0xFF);
            buffer.setChar(buffer.getCursorAddress(), ch);
            buffer.moveCursorRight();
        }
        return pos;
    }
    
    public int processCharacter(byte[] data, int pos) {
        if (pos < data.length) {
            char ch = (char) (data[pos++] & 0xFF);
            
            if (ch >= 0x40 && ch <= 0xFE) {
                ch = ebcdicToAscii(ch);
            }
            
            buffer.setChar(buffer.getCursorAddress(), ch);
            buffer.moveCursorRight();
        }
        return pos;
    }
    
    public int decodeAddress(byte high, byte low) {
        int h = high & 0xFF;
        int l = low & 0xFF;
        
        int address = ((h & 0x3F) << 6) | (l & 0x3F);
        
        return address;
    }
    
    public int[] getRowColumnFromAddress(int position) {
        int row = (position / buffer.getCols()) + 1;
        int col = (position % buffer.getCols()) + 1;
        return new int[] {row, col};
    }
    
    private char ebcdicToAscii(int ebcdic) {
        char[] conversionTable = {
            ' ', ' ', ' ', ' ', ' ', '\t', ' ', ' ',
            ' ', ' ', ' ', '\n', '\f', '\r', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', '\n', '\b', ' ',
            ' ', ' ', ' ', '\033', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', '\n', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', ' ', '.', '<', '(', '+', '|',
            '&', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', '!', '$', '*', ')', ';', '~',
            '-', '/', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '^', ',', '%', '_', '>', '?',
            ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', '`', ':', '#', '@', '\'', '=', '"',
            ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', ' ', ' ', ' ', ' ', ' ', ' ',
            ' ', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', ' ', ' ', ' ', ' ', ' ', ' ',
            '\\', ' ', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', ' ', ' ', ' ', ' ', ' ', ' ',
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', ' ', ' ', ' ', ' ', ' ', ' '
        };
        
        if (ebcdic >= 0x40 && ebcdic <= 0xFE) {
            return conversionTable[ebcdic - 0x40];
        }
        return (char) ebcdic;
    }
}
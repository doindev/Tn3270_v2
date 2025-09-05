package org.me.telnet.tn3270;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;

public class Screen {
    private ScreenBuffer buffer;
    private DataStreamBuilder builder;
    private OutputStream outputStream;
    
    public Screen(ScreenBuffer buffer) {
        this.buffer = buffer;
        this.builder = new DataStreamBuilder(buffer);
    }
    
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    public void sendCommandKey(CommandKey key) throws IOException {
        if (outputStream != null) {
            byte[] data = builder.build(key.getAidCode());
            outputStream.write(data);
            outputStream.flush();
            buffer.setLastAid(key.getAidCode());
        }
    }
    
    public Screen pa1() throws IOException {
        sendCommandKey(CommandKey.PA1);
        return this;
    }
    
    public Screen pa2() throws IOException {
        sendCommandKey(CommandKey.PA2);
        return this;
    }
    
    public Screen pa3() throws IOException {
        sendCommandKey(CommandKey.PA3);
        return this;
    }
    
    public Screen pf1() throws IOException {
        sendCommandKey(CommandKey.PF1);
        return this;
    }
    
    public Screen pf2() throws IOException {
        sendCommandKey(CommandKey.PF2);
        return this;
    }
    
    public Screen pf3() throws IOException {
        sendCommandKey(CommandKey.PF3);
        return this;
    }
    
    public Screen pf4() throws IOException {
        sendCommandKey(CommandKey.PF4);
        return this;
    }
    
    public Screen pf5() throws IOException {
        sendCommandKey(CommandKey.PF5);
        return this;
    }
    
    public Screen pf6() throws IOException {
        sendCommandKey(CommandKey.PF6);
        return this;
    }
    
    public Screen pf7() throws IOException {
        sendCommandKey(CommandKey.PF7);
        return this;
    }
    
    public Screen pf8() throws IOException {
        sendCommandKey(CommandKey.PF8);
        return this;
    }
    
    public Screen pf9() throws IOException {
        sendCommandKey(CommandKey.PF9);
        return this;
    }
    
    public Screen pf10() throws IOException {
        sendCommandKey(CommandKey.PF10);
        return this;
    }
    
    public Screen pf11() throws IOException {
        sendCommandKey(CommandKey.PF11);
        return this;
    }
    
    public Screen pf12() throws IOException {
        sendCommandKey(CommandKey.PF12);
        return this;
    }
    
    public Screen enter() throws IOException {
        sendCommandKey(CommandKey.ENTER);
        return this;
    }
    
    public Screen up() {
        buffer.moveCursorUp();
        return this;
    }
    
    public Screen down() {
        buffer.moveCursorDown();
        return this;
    }
    
    public Screen left() {
        buffer.moveCursorLeft();
        return this;
    }
    
    public Screen right() {
        buffer.moveCursorRight();
        return this;
    }
    
    public Screen tab() {
        return tab(false);
    }
    
    public Screen tab(boolean shiftEnabled) {
        if (shiftEnabled) {
            buffer.moveCursorToPreviousUnprotectedField();
        } else {
            buffer.moveCursorToNextUnprotectedField();
        }
        return this;
    }
    
    public Screen home() {
        // Move cursor to the first editable position on the screen
        int totalPositions = buffer.getRows() * buffer.getCols();
        
        // Start from position 0 and look for the first unprotected field
        for (int position = 0; position < totalPositions; position++) {
            InputField field = buffer.getFieldAt(position);
            if (field != null && field.canInput()) {
                // Found first editable field, set cursor to its start position
                buffer.setCursorPosition(field.startRow(), field.startColumn());
                return this;
            }
        }
        
        // If no editable field found, move to position 0,0
        buffer.setCursorPosition(0, 0);
        return this;
    }
    
    public Screen insert() {
        buffer.setInsertMode(!buffer.isInsertMode());
        return this;
    }
    
    public Screen delete() {
        InputField field = buffer.getFieldAt(buffer.getCursorRow(), buffer.getCursorColumn());
        if (field != null && field.canInput()) {
            field.deleteCharacter();
        }
        return this;
    }
    
    public Screen backspace() {
        InputField field = buffer.getFieldAt(buffer.getCursorRow(), buffer.getCursorColumn());
        if (field != null && field.canInput()) {
            field.backspace();
        }
        return this;
    }
    
    public Screen esc() throws IOException {
        sendCommandKey(CommandKey.ESC);
        return this;
    }
    
    public Screen clear() throws IOException {
        sendCommandKey(CommandKey.CLEAR);
        buffer.clear();
        return this;
    }
    
    public Screen pgup() {
//        int newRow = Math.max(1, buffer.getCursorRow() - 10);
//        buffer.setCursorPosition(newRow, buffer.getCursorColumn());
        return this;
    }
    
    public Screen pgdn() {
//        int newRow = Math.min(buffer.getRows(), buffer.getCursorRow() + 10);
//        buffer.setCursorPosition(newRow, buffer.getCursorColumn());
        return this;
    }
    
    public Screen keyPressed(KeyEvent e) {
        if (buffer.isKeyboardLocked()) {
            return this;
        }
        
        try {
            int keyCode = e.getKeyCode();
            boolean shift = e.isShiftDown();
//            boolean ctrl = e.isControlDown();
            
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    enter();
                    break;
                case KeyEvent.VK_TAB:
                    tab(shift);
                    break;
                case KeyEvent.VK_UP:
                    up();
                    break;
                case KeyEvent.VK_DOWN:
                    down();
                    break;
                case KeyEvent.VK_LEFT:
                    left();
                    break;
                case KeyEvent.VK_RIGHT:
                    right();
                    break;
                case KeyEvent.VK_DELETE:
                    delete();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    backspace();
                    break;
                case KeyEvent.VK_INSERT:
                    insert();
                    break;
                case KeyEvent.VK_ESCAPE:
                    esc();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    pgup();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    pgdn();
                    break;
                case KeyEvent.VK_F1:
                    pf1();
                    break;
                case KeyEvent.VK_F2:
                    pf2();
                    break;
                case KeyEvent.VK_F3:
                    pf3();
                    break;
                case KeyEvent.VK_F4:
                    pf4();
                    break;
                case KeyEvent.VK_F5:
                    pf5();
                    break;
                case KeyEvent.VK_F6:
                    pf6();
                    break;
                case KeyEvent.VK_F7:
                    pf7();
                    break;
                case KeyEvent.VK_F8:
                    pf8();
                    break;
                case KeyEvent.VK_F9:
                    pf9();
                    break;
                case KeyEvent.VK_F10:
                    pf10();
                    break;
                case KeyEvent.VK_F11:
                    pf11();
                    break;
                case KeyEvent.VK_F12:
                    pf12();
                    break;
                default:
                    char ch = e.getKeyChar();
                    if (!Character.isISOControl(ch)) {
                        putString(String.valueOf(ch));
                    }
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return this;
    }
    
    public Screen putString(String text) {
        if (buffer.isKeyboardLocked() || text == null || text.isEmpty()) {
            return this;
        }
        
        int currentRow = buffer.getCursorRow();
        int currentCol = buffer.getCursorColumn();
        int totalCols = buffer.getCols();
        int totalRows = buffer.getRows();
        
        for (char ch : text.toCharArray()) {
            // Get current position as address
            int currentAddress = currentRow * totalCols + currentCol;
            
            // Check if there's a field attribute at this position (can't overwrite field markers)
            FieldAttribute attrAtPos = buffer.getAttribute(currentRow, currentCol);
            if (attrAtPos != null) {
                // This is a field start position, skip to next position
                currentCol++;
                if (currentCol >= totalCols) {
                    currentCol = 0;
                    currentRow++;
                    if (currentRow >= totalRows) {
                        currentRow = 0;
                    }
                }
                currentAddress = currentRow * totalCols + currentCol;
            }
            
            // Find the field at current position
            InputField field = buffer.getFieldAt(currentRow, currentCol);
            
            if (field != null && field.canInput()) {
                // Check if character is valid for this field
                if (field.attribute().isNumeric() && !Character.isDigit(ch)) {
                    continue; // Skip non-numeric characters in numeric fields
                }
                
                // Calculate position within the field
                int fieldStartPos = field.getFieldPosition();
                int fieldEndRow = field.endRow();
                int fieldEndCol = field.endColumn();
                int fieldEndPos = fieldEndRow * totalCols + fieldEndCol;
                
                // Check if we're still within field boundaries
                if ((fieldEndPos >= fieldStartPos && currentAddress > fieldEndPos) ||
                    (fieldEndPos < fieldStartPos && currentAddress > fieldEndPos && currentAddress < fieldStartPos)) {
                    // We've reached the end of this field, try to move to next unprotected field
                    buffer.setCursorPosition(currentRow, currentCol);
                    buffer.moveCursorToNextUnprotectedField();
                    currentRow = buffer.getCursorRow();
                    currentCol = buffer.getCursorColumn();
                    
                    // Try again with the new field
                    field = buffer.getFieldAt(currentRow, currentCol);
                    if (field == null || !field.canInput()) {
                        break; // No more writable fields
                    }
                }
                
                // Write the character to the buffer
                if (buffer.isInsertMode()) {
                    // In insert mode, we need to shift characters to the right
                    // First, save all characters from current position to end of field
                    int remainingPositions = 0;
                    if (fieldEndPos >= currentAddress) {
                        remainingPositions = fieldEndPos - currentAddress;
                    } else {
                        // Field wraps around
                        remainingPositions = (totalRows * totalCols - currentAddress) + fieldEndPos;
                    }
                    
                    // Shift characters right within field boundaries
                    for (int i = remainingPositions; i > 0; i--) {
                        int srcPos = currentAddress + i - 1;
                        int dstPos = currentAddress + i;
                        
                        int srcRow = (srcPos / totalCols) % totalRows;
                        int srcCol = srcPos % totalCols;
                        int dstRow = (dstPos / totalCols) % totalRows;
                        int dstCol = dstPos % totalCols;
                        
                        // Don't shift beyond field boundary
                        if (dstPos <= fieldEndPos || (fieldEndPos < fieldStartPos && dstPos <= fieldEndPos + totalRows * totalCols)) {
                            char srcChar = buffer.getChar(srcRow, srcCol);
                            buffer.setChar(dstRow, dstCol, srcChar);
                        }
                    }
                }
                
                // Write the character to the buffer at current position
                buffer.setChar(currentRow, currentCol, ch);
                
                // Also update the field's internal data
                field.replaceCharacter(ch);
                
                // Move to next position
                currentCol++;
                if (currentCol >= totalCols) {
                    currentCol = 0;
                    currentRow++;
                    if (currentRow >= totalRows) {
                        currentRow = 0;
                    }
                }
            } else if (field == null) {
                // No field at this position, write directly to buffer if not protected
                buffer.setChar(currentRow, currentCol, ch);
                
                // Move to next position
                currentCol++;
                if (currentCol >= totalCols) {
                    currentCol = 0;
                    currentRow++;
                    if (currentRow >= totalRows) {
                        currentRow = 0;
                    }
                }
            } else {
                // Protected field, try to move to next unprotected field
                buffer.setCursorPosition(currentRow, currentCol);
                buffer.moveCursorToNextUnprotectedField();
                currentRow = buffer.getCursorRow();
                currentCol = buffer.getCursorColumn();
                
                // Try to write the character at the new position
                field = buffer.getFieldAt(currentRow, currentCol);
                if (field != null && field.canInput()) {
                    // Retry writing this character at the new position
                    buffer.setChar(currentRow, currentCol, ch);
                    field.replaceCharacter(ch);
                    
                    // Move to next position
                    currentCol++;
                    if (currentCol >= totalCols) {
                        currentCol = 0;
                        currentRow++;
                        if (currentRow >= totalRows) {
                            currentRow = 0;
                        }
                    }
                } else {
                    break; // No more writable positions
                }
            }
        }
        
        // Update cursor position
        buffer.setCursorPosition(currentRow, currentCol);
        
        return this;
    }
    
    public Screen putString(int cursorPosition, String text) {
        // Convert position to row/column
        int row = cursorPosition / buffer.getCols();
        int col = cursorPosition % buffer.getCols();
        buffer.setCursorPosition(row, col);
        return putString(text);
    }
    
    public Screen putString(int row, int column, String text) {
        buffer.setCursorPosition(row, column);
        return putString(text);
    }
    
    public String getString() {
        return String.join("\n", buffer.getAllRows());
    }
    
    public String getString(String separator) {
        return String.join(separator, buffer.getAllRows());
    }
    
    public String getString(int row) {
        return buffer.getRowAsString(row);
    }
    
    public String getString(int cursorPosition, int length) {
        StringBuilder result = new StringBuilder();
        int totalCols = buffer.getCols();
        
        for (int i = 0; i < length; i++) {
            int pos = cursorPosition + i;
            int row = (pos / totalCols);
            int col = (pos % totalCols);
            
            if (row > buffer.getRows()) {
                break;
            }
            
            result.append(buffer.getChar(row, col));
        }
        
        return result.toString();
    }
    
    public String getString(int row, int column, int length) {
        int cursorPosition = row * buffer.getCols() + column;
        return getString(cursorPosition, length);
    }
    
    public InputField[] getFields() {
        return buffer.getFields().toArray(new InputField[0]);
    }
    
    public InputField getFieldAt(int cursorPosition) {
        return buffer.getFieldAt(cursorPosition);
    }
    
    public FieldAttribute getFieldAttributeAt(int cursorPosition) {
        InputField field = getFieldAt(cursorPosition);
        return field != null ? field.attribute() : null;
    }
    
    public FieldAttribute getFieldAttributeAt(int row, int column) {
        InputField field = buffer.getFieldAt(row, column);
        return field != null ? field.attribute() : null;
    }
    
    public ScreenBuffer getBuffer() {
        return buffer;
    }
}
package org.metoo.telnet.tn3270;

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
        
        // If no editable field found, move to position 1,1
        buffer.setCursorPosition(1, 1);
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
        int newRow = Math.max(1, buffer.getCursorRow() - 10);
        buffer.setCursorPosition(newRow, buffer.getCursorColumn());
        return this;
    }
    
    public Screen pgdn() {
        int newRow = Math.min(buffer.getRows(), buffer.getCursorRow() + 10);
        buffer.setCursorPosition(newRow, buffer.getCursorColumn());
        return this;
    }
    
    public Screen keyPressed(KeyEvent e) {
        if (buffer.isKeyboardLocked()) {
            return this;
        }
        
        try {
            int keyCode = e.getKeyCode();
            boolean shift = e.isShiftDown();
            boolean ctrl = e.isControlDown();
            
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
        if (buffer.isKeyboardLocked() || text == null) {
            return this;
        }
        
        InputField field = buffer.getFieldAt(buffer.getCursorRow(), buffer.getCursorColumn());
        if (field != null && field.canInput()) {
            for (char ch : text.toCharArray()) {
                if (field.attribute().isNumeric() && !Character.isDigit(ch)) {
                    continue;
                }
                
                if (buffer.isInsertMode()) {
                    field.insertCharacter(ch);
                } else {
                    field.replaceCharacter(ch);
                }
            }
        }
        
        return this;
    }
    
    public Screen putString(int cursorPosition, String text) {
        buffer.setCursorAddress(cursorPosition);
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
            int row = (pos / totalCols) + 1;
            int col = (pos % totalCols) + 1;
            
            if (row > buffer.getRows()) {
                break;
            }
            
            result.append(buffer.getChar(row, col));
        }
        
        return result.toString();
    }
    
    public String getString(int row, int column, int length) {
        int cursorPosition = (row - 1) * buffer.getCols() + (column - 1);
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
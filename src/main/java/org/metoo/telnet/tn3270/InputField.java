package org.metoo.telnet.tn3270;

import java.util.Arrays;

public class InputField {
    private int startRow;
    private int startColumn;
    private int endRow;
    private int endColumn;
    private int length;
    private FieldAttribute attribute;
    private char[] data;
    private int cursorOffset;
    private boolean modified;
    
    public InputField(int startRow, int startColumn, int endRow, int endColumn) {
        this(startRow, startColumn, endRow, endColumn, new FieldAttribute());
    }
    
    public InputField(int startRow, int startColumn, int length) {
        this(startRow, startColumn, length, new FieldAttribute());
    }
    
    public InputField(int startRow, int startColumn, int length, FieldAttribute attribute) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.length = length;
        this.attribute = attribute;
        this.data = new char[length];
        Arrays.fill(this.data, ' ');
        this.cursorOffset = 0;
        this.modified = false;
        
        int totalColumns = 80;
        int totalPosition = (startRow - 1) * totalColumns + (startColumn - 1) + length - 1;
        this.endRow = (totalPosition / totalColumns) + 1;
        this.endColumn = (totalPosition % totalColumns) + 1;
    }
    
    public InputField(int startRow, int startColumn, int endRow, int endColumn, FieldAttribute attribute) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
        this.attribute = attribute;
        
        int totalColumns = 80;
        int startPos = (startRow - 1) * totalColumns + (startColumn - 1);
        int endPos = (endRow - 1) * totalColumns + (endColumn - 1);
        this.length = endPos - startPos + 1;
        this.data = new char[length];
        Arrays.fill(this.data, ' ');
        this.cursorOffset = 0;
        this.modified = false;
    }
    
    public boolean containsPosition(int row, int col) {
        int totalColumns = 80;
        int position = (row - 1) * totalColumns + (col - 1);
        int startPos = (startRow - 1) * totalColumns + (startColumn - 1);
        int endPos = (endRow - 1) * totalColumns + (endColumn - 1);
        
        return position >= startPos && position <= endPos;
    }
    
    public int getFieldPosition() {
        int totalColumns = 80;
        return (startRow - 1) * totalColumns + (startColumn - 1);
    }
    
    public boolean canInput() {
        return attribute.canInput();
    }
    
    public boolean insertCharacter(char ch) {
        if (!canInput() || cursorOffset >= length) {
            return false;
        }
        
        if (cursorOffset < length - 1) {
            System.arraycopy(data, cursorOffset, data, cursorOffset + 1, length - cursorOffset - 1);
        }
        
        data[cursorOffset] = ch;
        modified = true;
        attribute.modified(true);
        cursorOffset++;
        return true;
    }
    
    public boolean replaceCharacter(char ch) {
        if (!canInput() || cursorOffset >= length) {
            return false;
        }
        
        data[cursorOffset] = ch;
        modified = true;
        attribute.modified(true);
        cursorOffset++;
        return true;
    }
    
    public boolean deleteCharacter() {
        if (!canInput() || cursorOffset >= length) {
            return false;
        }
        
        if (cursorOffset < length - 1) {
            System.arraycopy(data, cursorOffset + 1, data, cursorOffset, length - cursorOffset - 1);
            data[length - 1] = ' ';
        } else {
            data[cursorOffset] = ' ';
        }
        
        modified = true;
        attribute.modified(true);
        return true;
    }
    
    public boolean backspace() {
        if (!canInput() || cursorOffset <= 0) {
            return false;
        }
        
        cursorOffset--;
        if (cursorOffset < length - 1) {
            System.arraycopy(data, cursorOffset + 1, data, cursorOffset, length - cursorOffset - 1);
            data[length - 1] = ' ';
        } else {
            data[cursorOffset] = ' ';
        }
        
        modified = true;
        attribute.modified(true);
        return true;
    }
    
    public String getData() {
        return new String(data);
    }
    
    public String getDataTrimmed() {
        return new String(data).trim();
    }
    
    public void clearData() {
        Arrays.fill(data, ' ');
        cursorOffset = 0;
        modified = false;
        attribute.modified(false);
    }
    
    public boolean canMoveCursorLeft() {
        return cursorOffset > 0;
    }
    
    public boolean canMoveCursorRight() {
        return cursorOffset < length - 1;
    }
    
    public boolean moveCursorRight() {
        if (canMoveCursorRight()) {
            cursorOffset++;
            return true;
        }
        return false;
    }
    
    public boolean moveCursorLeft() {
        if (canMoveCursorLeft()) {
            cursorOffset--;
            return true;
        }
        return false;
    }
    
    public void setCursorOffset(int offset) {
        if (offset >= 0 && offset < length) {
            this.cursorOffset = offset;
        }
    }
    
    public int startRow() {
        return startRow;
    }
    
    public int startColumn() {
        return startColumn;
    }
    
    public int endRow() {
        return endRow;
    }
    
    public int endColumn() {
        return endColumn;
    }
    
    public int length() {
        return length;
    }
    
    public int cursorOffset() {
        return cursorOffset;
    }
    
    public InputField attribute(FieldAttribute attribute) {
        this.attribute = attribute;
        return this;
    }
    
    public FieldAttribute attribute() {
        return attribute;
    }
    
    public boolean modified() {
        return modified || attribute.isModified();
    }
    
    public InputField modified(boolean b) {
        this.modified = b;
        attribute.modified(b);
        return this;
    }
}
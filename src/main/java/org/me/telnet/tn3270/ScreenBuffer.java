package org.me.telnet.tn3270;

import java.util.ArrayList;
import java.util.List;

public class ScreenBuffer {
    private static final int DEFAULT_ROWS = 24;
    private static final int DEFAULT_COLS = 80;
    
    private int rows;
    private int cols;
    private char[][] buffer;
    private FieldAttribute[][] attributes;
    private List<InputField> fields;
    private int cursorRow;
    private int cursorColumn;
    private int cursorAddress;
    private boolean keyboardLocked;
    private boolean insertMode;
    private byte lastAid;
    private int bufferRow;
    private int bufferColumn;
    private int bufferAddress;
    
    
    public ScreenBuffer() {
        this(DEFAULT_ROWS, DEFAULT_COLS);
    }
    
    public ScreenBuffer(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.buffer = new char[rows][cols];
        this.attributes = new FieldAttribute[rows][cols];
        this.fields = new ArrayList<>();
        this.bufferRow = 0;
        this.bufferColumn = 0;
        this.bufferAddress = 0;
        this.cursorRow = 0;
        this.cursorColumn = 0;
        this.cursorAddress = 0;
        this.keyboardLocked = false;
        this.insertMode = false;
        this.lastAid = 0;
        
        clear();
    }
    
    public void clear() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buffer[i][j] = ' ';
                attributes[i][j] = null;
            }
        }
        fields.clear();
        bufferRow = 0;
        bufferColumn = 0;
        bufferAddress = 0;
        cursorRow = 0;
        cursorColumn = 0;
        cursorAddress = 0;
    }
    
    public boolean isKeyboardLocked() {
        return keyboardLocked;
    }
    
    public void setKeyboardLocked(boolean locked) {
        this.keyboardLocked = locked;
    }
    
    public char getChar(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return buffer[row][col];
        }
        return ' ';
    }
    
    public void setChar(int row, int col, char ch) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            buffer[row][col] = ch;
        }
    }
    
    public void setChar(int position, char ch) {
        int row = (position / cols);
        int col = (position % cols);
        setChar(row, col, ch);
    }
    
    public FieldAttribute getAttribute(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return attributes[row][col];
        }
        return null;
    }
    
    public void setAttribute(int row, int col, FieldAttribute attr) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            attributes[row][col] = attr;
        }
    }
    
    public void setAttribute(int position, FieldAttribute attr) {
        int row = (position / cols);
        int col = (position % cols);
        setAttribute(row, col, attr);
    }
    
    public List<InputField> getFields() {
        return new ArrayList<>(fields);
    }
    
    public void addField(InputField field) {
        fields.add(field);
    }
    
    public void clearFields() {
        fields.clear();
    }
    
    public InputField getFieldAt(int row, int col) {
        for (InputField field : fields) {
            if (field.containsPosition(row, col)) {
                return field;
            }
        }
        return null;
    }
    
    public InputField getFieldAt(int position) {
        int row = (position / cols);
        int col = (position % cols);
        return getFieldAt(row, col);
    }
    
    public int getCursorRow() {
        return cursorRow;
    }
    
    public int getBufferRow() {
        return bufferRow;
    }
    
    public int getCursorColumn() {
        return cursorColumn;
    }
    
    public int getBufferColumn() {
        return bufferColumn;
    }
    
    public void setCursorPosition(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            this.cursorRow = row;
            this.cursorColumn = col;
            this.cursorAddress = row * cols + col;
        }
    }
    
    public void setBufferPosition(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            this.bufferRow = row;
            this.bufferColumn = col;
            this.bufferAddress = row * cols + col;
        }
    }
    
    public void setCursorAddress(int address) {
        if (address >= 0 && address < rows * cols) {
            this.cursorAddress = address;
            this.cursorRow = (address / cols);
            this.cursorColumn = (address % cols);
        }
    }
    
    public void setBufferAddress(int address) {
        if (address >= 0 && address < rows * cols) {
            this.bufferAddress = address;
            this.bufferRow = (address / cols);
            this.bufferColumn = (address % cols);
        }
    }
    
    public int getCursorAddress() {
        return cursorAddress;
    }
    
    public int getBufferAddress() {
        return bufferAddress;
    }
    
    public boolean moveCursorUp() {
        if (cursorRow > 1) {
            setCursorPosition(cursorRow - 1, cursorColumn);
            return true;
        } else if (cursorRow == 0) {
        	setCursorPosition(rows -1, cursorColumn);
        	return true;
        }
        return false;
    }
    
    public boolean moveBufferUp() {
        if (bufferRow > 1) {
            setBufferPosition(bufferRow - 1, bufferColumn);
            return true;
        } else if (bufferRow == 0) {
        	setBufferPosition(rows -1, bufferColumn);
        	return true;
        }
        return false;
    }
    
    public boolean moveCursorDown() {
        if (cursorRow < rows) {
            setCursorPosition(cursorRow + 1, cursorColumn);
            return true;
        } else if (cursorRow == (rows-1)) {
			setCursorPosition(0, cursorColumn);
			return true;
		}
        return false;
    }
    
    public boolean moveBufferDown() {
        if (bufferRow < rows) {
            setBufferPosition(bufferRow + 1, bufferColumn);
            return true;
        } else if (bufferRow == (rows-1)) {
			setBufferPosition(0, bufferColumn);
			return true;
		}
        return false;
    }
    
    public boolean moveCursorLeft() {
        if (cursorColumn > 0) {
            setCursorPosition(cursorRow, cursorColumn - 1);
            return true;
        } else if (cursorRow > 0) {
            setCursorPosition(cursorRow - 1, cols -1);
            return true;
        } else if (cursorRow == 0 && cursorColumn == 0) {
        	setCursorPosition(rows -1, cols -1);
        	return true;
        }
        return false;
    }
    
    public boolean moveBufferLeft() {
        if (bufferColumn > 0) {
            setBufferPosition(bufferRow, bufferColumn - 1);
            return true;
        } else if (bufferRow > 0) {
            setBufferPosition(bufferRow - 1, cols -1);
            return true;
        } else if (bufferRow == 0 && bufferColumn == 0) {
        	setBufferPosition(rows -1, cols -1);
        	return true;
        }
        return false;
    }
    
    public boolean moveCursorRight() {
        if (cursorColumn < cols) {
            setCursorPosition(cursorRow, cursorColumn + 1);
            return true;
        } else if (cursorRow < rows) {
            setCursorPosition(cursorRow + 1, 0);
            return true;
        } else if (cursorRow == (rows -1) && cursorColumn == (cols-1)) {
			setCursorPosition(0, 0);
			return true;
		}
        return false;
    }
    
    public boolean moveBufferRight() {
        if (bufferColumn < cols) {
            setBufferPosition(bufferRow, bufferColumn + 1);
            return true;
        } else if (bufferRow < rows) {
            setBufferPosition(bufferRow + 1, 0);
            return true;
        } else if (bufferRow == (rows -1) && bufferColumn == (cols-1)) {
			setBufferPosition(0, 0);
			return true;
		}
        return false;
    }
    
    public void moveCursorToNextUnprotectedField() {
        int startAddress = cursorAddress;
        int currentAddress = (startAddress + 1) % (rows * cols);
        
        while (currentAddress != startAddress) {
            InputField field = getFieldAt(currentAddress);
            if (field != null && field.canInput()) {
                setCursorAddress(currentAddress);
                return;
            }
            currentAddress = (currentAddress + 1) % (rows * cols);
        }
    }
    
    public void moveBufferToNextUnprotectedField() {
        int startAddress = bufferAddress;
        int currentAddress = (startAddress + 1) % (rows * cols);
        
        while (currentAddress != startAddress) {
            InputField field = getFieldAt(currentAddress);
            if (field != null && field.canInput()) {
                setBufferAddress(currentAddress);
                return;
            }
            currentAddress = (currentAddress + 1) % (rows * cols);
        }
    }
    
    public void moveCursorToPreviousUnprotectedField() {
        int startAddress = cursorAddress;
        int currentAddress = (startAddress - 1 + rows * cols) % (rows * cols);
        
        while (currentAddress != startAddress) {
            InputField field = getFieldAt(currentAddress);
            if (field != null && field.canInput()) {
                setCursorAddress(currentAddress);
                return;
            }
            currentAddress = (currentAddress - 1 + rows * cols) % (rows * cols);
        }
    }
    
    public void moveBufferToPreviousUnprotectedField() {
        int startAddress = bufferAddress;
        int currentAddress = (startAddress - 1 + rows * cols) % (rows * cols);
        
        while (currentAddress != startAddress) {
            InputField field = getFieldAt(currentAddress);
            if (field != null && field.canInput()) {
                setBufferAddress(currentAddress);
                return;
            }
            currentAddress = (currentAddress - 1 + rows * cols) % (rows * cols);
        }
    }
    
    public String getRowAsString(int row) {
        if (row >= 1 && row <= rows) {
            return new String(buffer[row - 1]);
        }
        return "";
    }
    
    public String[] getAllRows() {
        String[] result = new String[rows];
        for (int i = 0; i < rows; i++) {
            result[i] = new String(buffer[i]);
        }
        return result;
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getCols() {
        return cols;
    }
    
    public boolean isInsertMode() {
        return insertMode;
    }
    
    public void setInsertMode(boolean insertMode) {
        this.insertMode = insertMode;
    }
    
    public byte getLastAid() {
        return lastAid;
    }
    
    public void setLastAid(byte lastAid) {
        this.lastAid = lastAid;
    }
    
    public void resetModifiedFlags() {
        for (InputField field : fields) {
            field.modified(false);
        }
    }
    
    public List<InputField> getModifiedFields() {
        List<InputField> modifiedFields = new ArrayList<>();
        for (InputField field : fields) {
            if (field.modified()) {
                modifiedFields.add(field);
            }
        }
        return modifiedFields;
    }
    
    public void writeString(String text, int row, int col) {
    	// todo: this logic needs to be improved, should test if can write in field, moving to the next field when full, insert mode, etc
        for (int i = 0; i < text.length() && col + i <= cols; i++) {
            setChar(row, col + i, text.charAt(i));
        }
    }
    
    public void writeString(String text) {
    	//todo: this logic needs to be improved, should test if can write in field, moving to next field when current field full, insert mode, etc
        writeString(text, cursorRow, cursorColumn);
        int newCol = cursorColumn + text.length();
        if (newCol <= cols) {
            cursorColumn = newCol;
        } else {
            cursorColumn = cols;
        }
    }
}
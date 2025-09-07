package org.me.telnet.tn3270;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ScreenBuffer {
	private static final int DEFAULT_ROWS = 24;
    private static final int DEFAULT_COLS = 80;
     
    private int rows;
    private int cols;
    private int total;
    
    private char[] asciiBuffer;
    private FieldAttribute[] attributeBuffer;
    private Integer[] fieldBuffer;
    
    private Integer currentField;
    private FieldAttribute currentAttribute;
    
    private int cursorPosition;
    private int bufferPosition;
    
    private boolean hasFields;
    private boolean isInsertMode;
    private boolean keyboardLocked;
    
    public ScreenBuffer() {
		this(DEFAULT_ROWS, DEFAULT_COLS);
	}
    
    public ScreenBuffer(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.total = rows * cols;
		
		this.asciiBuffer = new char[total];
		this.attributeBuffer = new FieldAttribute[total];
		this.fieldBuffer = new Integer[total];
		
		clear();
    }
    
    public void setKeyboardLocked(boolean locked) {
		this.keyboardLocked = locked;
	}
    public boolean isKeyboardLocked() {return keyboardLocked;}
    
    public void setBufferPosition(int pos) {
		if(pos>=0 && pos<total) {
			this.bufferPosition = pos;
		}
	}
    public int getBufferPosition() {return bufferPosition;}
    
    public void setCurrentField(int position) {
		this.currentField = position;
    }
    public Integer getCurrentField() {return currentField;}
    
    public void startField(int position, byte attribute) {
    	this.hasFields = true;
    	this.currentField = position;
    	this.currentAttribute = new FieldAttribute(attribute);
    	this.attributeBuffer[position] = currentAttribute;
    	this.fieldBuffer[position] = position;
    }
    public boolean hasFields() {return hasFields;}
    
    public int getCursorPosition() {
		return cursorPosition;
	}
    public void setCursorPosition(int position) { cursorPosition = position; }

    public FieldAttribute getAttribute(int position) {
    	return (attributeBuffer[position]==null?new FieldAttribute():attributeBuffer[position]);// just return default if null
    }
    public Integer getField(int position) {
		return fieldBuffer[position];
	}
    
    public int getCols() {return cols;}
    public int getRows() {return rows;}
    public int getTotal() {return total;}
    public char asciiAt(int position) {
    	return asciiBuffer[position];
    }
    
    public void clear() {
    	Arrays.fill(asciiBuffer, ' ');
    	Arrays.fill(attributeBuffer, null);
    	Arrays.fill(fieldBuffer, null);
    	
    	currentField = null;
    	currentAttribute = null;
    	
    	cursorPosition = 0;
    	bufferPosition = 0;
    }
    
    public int pushEbcdic(byte b) {
    	asciiBuffer[bufferPosition] = Tn3270Conversions.ebcdicToAscii(b);
    	attributeBuffer[bufferPosition] = currentAttribute;
    	fieldBuffer[bufferPosition] = currentField;
    	
    	return incBufferPosition();
	}
    
    public int pushAscii(char c) {
//    	System.out.println("hasFields: " + hasFields + " cursorPosition: " + cursorPosition + " char: '" + c + "'");
    	if(hasFields) {
    		// if field attribute is not already modified then set it
    		if(!attributeBuffer[cursorPosition].isModified()) {
    			attributeBuffer[cursorPosition].modified(true);
			}
    		
    		// maybe fix
    		if(!attributeBuffer[fieldBuffer[cursorPosition]].isModified()) {
    			attributeBuffer[fieldBuffer[cursorPosition]].modified(true);
			}

    		// since this buffer hasFields we can only modify field data
    		if(
    			fieldBuffer[cursorPosition] != null &&
    			fieldBuffer[cursorPosition] != cursorPosition &&
    			!attributeBuffer[fieldBuffer[cursorPosition]].isProtected() // FieldType.UNPROTECTED
    		) {
//    			System.out.println("field is unprotected");
				int fieldEnd = cursorPosition;
    			if(isInsertMode) {
    				// find the end of the field
					for(int i=cursorPosition;i<(total-1);i++) {
						if(fieldBuffer[cursorPosition].intValue() == fieldBuffer[i].intValue()){
							fieldEnd = i - cursorPosition;
						}else{
							break;
						}
					}
					
					// insert mode is on so move everything 1 place to the right
					for(int i=cursorPosition+fieldEnd;i>cursorPosition;i--){
						asciiBuffer[i] = asciiBuffer[i-1];
					}
				}
    			
    			// the value is not suppose to be visible, like passwords
    			if(!attributeBuffer[cursorPosition].isVisible()) {
    				asciiBuffer[cursorPosition] = '*';
				} else {
					asciiBuffer[cursorPosition] = c;
    			}
    			
    			incCursorPosition();
			}
    	} else if( !attributeBuffer[cursorPosition].isProtected() ) {
    		// if field attribute is not already modified then set it
    		if(!attributeBuffer[cursorPosition].isModified()) {
    			attributeBuffer[cursorPosition].modified(true);
			}
    		
			if( !attributeBuffer[cursorPosition].isVisible() ) {
				asciiBuffer[cursorPosition] = '*';
			} else {
				asciiBuffer[cursorPosition] = c;
			}
			
			incCursorPosition();
		}
    	
    	return cursorPosition;
    }
    
    protected int incCursorPosition(){
		if(fieldBuffer[cursorPosition] != null){
			if(cursorPosition<(total-1)){
				if(fieldBuffer[cursorPosition + 1] != null && fieldBuffer[cursorPosition + 1].intValue() == fieldBuffer[cursorPosition].intValue()){
					cursorPosition++;
				}else{
					doTab(false);
				}
			}else{
				doTab(false);
			}
		}else if(!hasFields){
			cursorPosition++;
			if(cursorPosition>=total) {
				cursorPosition = 0;
			}
		}
		return cursorPosition;
	}
    
    protected int incBufferPosition() {
    	bufferPosition++;
		if(bufferPosition >= total) {
			bufferPosition=0;
		}
		
		return bufferPosition;
	}
    
    public int cursorXPosition(){return cursorPosition % cols;}
	public int cursorYPosition(){return cursorPosition / cols;}
	public int cursorPosition(){
		return cursorPosition;
	}
	public void cursorPosition(int cursorPosition) {
		if(cursorPosition<total && cursorPosition>=0){
			this.cursorPosition = cursorPosition;
		}
	}
	
	protected int doBackspace(){
		if(cursorPosition>0){
			if(hasFields){
				if(fieldBuffer[cursorPosition] !=null
					&& fieldBuffer[cursorPosition - 1] != null
					&& fieldBuffer[cursorPosition].intValue() == fieldBuffer[cursorPosition - 1].intValue()
					&& fieldBuffer[cursorPosition].intValue() != cursorPosition-1
					&& !attributeBuffer[cursorPosition].isProtected()
					&& !attributeBuffer[cursorPosition].isAutoSkip()
				){
					cursorPosition--;
					doDelete();
				}
			} else if(cursorPosition>0){
				if(attributeBuffer[cursorPosition-1].isModified()) {
					cursorPosition--;
					asciiBuffer[cursorPosition] = ' ';
					int x = 0;
					while(attributeBuffer[cursorPosition + x].isModified()) {
						x++;
					}
					
					for(int y = cursorPosition;y<cursorPosition+x;y++){
						asciiBuffer[y] = asciiBuffer[y+1];
					}
				}
			}
		}
		
		return cursorPosition;
	}
	
	protected int doDelete(){
		if(hasFields){
			if(
				fieldBuffer[cursorPosition] !=null
				&& !attributeBuffer[cursorPosition].isProtected()
				&& !attributeBuffer[cursorPosition].isAutoSkip()
			){
				if(!attributeBuffer[cursorPosition].isModified()) {
					attributeBuffer[cursorPosition].modified(true);
				}
				
				int fieldEnd = cursorPosition;
				for(int i=cursorPosition;i<1920;i++){
					if(fieldBuffer[cursorPosition].intValue() == fieldBuffer[i+1].intValue()){
						asciiBuffer[i] = asciiBuffer[i+1];
						attributeBuffer[i] = attributeBuffer[cursorPosition];
						fieldEnd = i+1;
					}else{
						break;
					}
				}
				if(cursorPosition < fieldEnd){
					asciiBuffer[fieldEnd] = ' ';
					attributeBuffer[fieldEnd] = attributeBuffer[cursorPosition];
				}
			}
		}else{
			int x = 0;
			while(attributeBuffer[cursorPosition + x].isModified()) {
				x++;
			}
			
			for(int y = cursorPosition;y<cursorPosition+x;y++){
				asciiBuffer[y] = asciiBuffer[y+1];
			}
		}
		return cursorPosition;
	}	
	
	protected boolean doInsert(){
		isInsertMode = !isInsertMode;
		return isInsertMode;
	}
	
	protected int doHome(){
		if(hasFields){
			cursorPosition = 0;
			doTab(false);
		}else{
			cursorPosition = 0;
		}
		return cursorPosition;
	}
	
	protected int doDown(){
		if(cursorPosition<(total - cols)){
			cursorPosition+=cols;
		}else{
			cursorPosition = cursorPosition % cols;
		}
		return cursorPosition;
	}
	
	protected int doUp(){
		if(cursorPosition - cols >= 0){
			cursorPosition-=cols;
		}else{
			cursorPosition = total - cols + (cursorPosition % cols);
		}
		return cursorPosition;
	}
	
	protected int doLeft(){
		if(cursorPosition>0){
			cursorPosition-=1;
		}else{
			cursorPosition = total -1;
		}
		return cursorPosition;
	}
	
	protected int doRight(){
		if(cursorPosition<(total -1)){
			cursorPosition+=1;
		}else{
			cursorPosition = 0;
		}
		return cursorPosition;
	}
	
	protected int doTab(boolean shift){
		if(hasFields){
			Field[] fields = fields();
			if(shift) {
				Arrays.sort(fields);
			}
			
			Field f = null;
			
			for(Field field:fields){
				if(!shift){
					if(
						field.position() > cursorPosition &&
						!field.attribute().isProtected() &&
						!field.attribute().isAutoSkip() &&
						field.length() > 0
					){
						f = field;
						break;
					}
				}else{
					if( 
						field.position()+1 < cursorPosition &&
						!field.attribute().isProtected() &&
						!field.attribute().isAutoSkip() &&
						field.length() > 0
					){
						f = field;
						break;
					}
				}
			}

			if(f == null){
				for(Field field:fields){
					if(
						!field.attribute().isProtected() &&
						!field.attribute().isAutoSkip() &&
						field.length() > 0
					){
						f = field;
						break;
					}
				}
			}
			
			if(f != null){
				cursorPosition = f.position() + 1;
			}
		}
		return cursorPosition;
	}
	
	protected void eraseAllUnprotected() {
		for(int i=0;i<total;i++){
			if(fieldBuffer[i] != null){
				for(int x = i+1;x<total;x++){
					if(x == fieldBuffer[x]){
						break;
					} else {
						i++;
					}

					if(
						attributeBuffer[fieldBuffer[x]].isProtected() && 
						attributeBuffer[fieldBuffer[x]].isNumeric()
					){
//						do nothing
					} else {
						asciiBuffer[x] = ' ';
					} 
				}
			}
		}
	}
	
	protected void resetModifiedFlags() {
		for(int i=0;i<total;i++){
			if(
				attributeBuffer[i] != null && 
				attributeBuffer[i].isModified() &&
				!attributeBuffer[i].isProtected()
			){
				attributeBuffer[i].modified(false);
			}
		}
	}
	
	
	
	
	
	public String string() {
		return string(null);
	}
	public String string(String separator) {
		StringBuffer sb = new StringBuffer("");
		
		if(separator!=null){
			for(int row=0;row<rows;row++){
				sb.append( string( row ) );
				sb.append(separator);
			}
		}else{
			try{
				sb.append( new String(asciiBuffer, 0, total) );
			}catch(Exception e){
				// catching error for unsupported ascii character
				for(int row=0;row<rows;row++) {
					sb.append( string( row ) );
				}
			}
		}

		return sb.toString();
	}
	public String string(int row) {
		return string((row*cols),cols);
	}
	public String string(int x, int y,int length) {
		return string((y*cols)+x, length);
	}
	public String string(int start, int length) {
		try{
    		return new String(asciiBuffer, start, length);
    	}catch(Exception e){
    		StringBuffer sb = new StringBuffer();
    		
    		for(int i=start;i<start+length;i++){
				try{
					sb.append(new String(asciiBuffer, i, 1));
				}catch(Exception ee){
					sb.append(" ");
				}
			}
    		return sb.toString();
    	}
	}
	
	protected Field[] fields() {
		return fields(false);
	}
	protected Field[] fields(boolean onlyModifiable) {
		ArrayList<Field> fields = new ArrayList<>();

		StringBuffer sb = new StringBuffer("");
		
		if(hasFields){
			for(int sf=0;sf<total;sf++) {
				if(fieldBuffer[sf] != null && fieldBuffer[sf].intValue() == sf){
//					System.out.println("Field start at position: " + sf + " attribute: " + attributeBuffer[sf]);
					sb = new StringBuffer("");
					
					if( asciiBuffer[sf] != ' ' ) {
//						System.out.println("sf sb.append(" + asciiBuffer[sf] + ") " + sf);
						sb.append( asciiBuffer[sf] );
					}
					
					for(int ef=sf+1;ef<=total;ef++) {						
						if(ef == total){
//							System.out.println("ef==total");
							fields.add( new Field(sf, attributeBuffer[sf], sb.toString()) );
							sf = ef;
						} else if(fieldBuffer[ef] != null && fieldBuffer[ef].intValue() != fieldBuffer[sf].intValue()){
//							System.out.println("fieldBuffer[ef] != null && fieldBuffer[ef].intValue() != fieldBuffer[sf].intValue()");
							if(asciiBuffer[sf] != ' '){
//								System.out.println(string(sf , ef -1 -sf));
								fields.add( new Field(sf, attributeBuffer[sf], string(sf , ef -1 -sf) ) );
							}else{
//								System.out.println(string(sf +1 , ef -1 -sf));
								fields.add( new Field(sf, attributeBuffer[sf], string(sf +1 , ef -1 -sf) ) );
							}
							
							sf = ef-1;
							break;
						} else {
//							System.out.println("ef sb.append(" + asciiBuffer[ef] + ") " + ef);
							sb.append( asciiBuffer[ef] );
//							System.out.println(ef + " : " + sb);
						}
					}
				}
			}	
		}
	
		if(!fields.isEmpty()){
			hasFields = true;
			Collections.sort(fields);
			return fields.toArray(new Field[fields.size()]);
		}else{
			return null;
		}
	}
	
	public static class Field implements Comparable<Field> {
		private FieldAttribute attribute;
		private int position;
		private String value;
		private int length;
		
		public Field(int position, FieldAttribute attribute, String asciiValue){
			this.attribute = attribute;
			this.position = position;
			this.value = asciiValue;
			if(this.value!=null) {
				this.length = this.value.length();
			}
		}
		
		public FieldAttribute attribute(){return attribute;}
		public String asciiValue() {return value;}
		public int length(){return length;}
		public int position(){return position;}
		
		@Override
		public int compareTo(Field field) {
			return this.position - field.position;
		}
		
		@Override
		public String toString() {
			return "Field [position=" + position + ", length=" + length + ", value=" + value + ", attribute=" + attribute
					+ "]";
		}
	}
}

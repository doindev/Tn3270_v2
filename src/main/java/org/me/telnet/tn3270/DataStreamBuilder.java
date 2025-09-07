package org.me.telnet.tn3270;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.me.telnet.TelnetCommand;
import org.me.telnet.tn3270.ScreenBuffer.Field;


public class DataStreamBuilder {
	private static final byte ORDER_SET_BUFFER_ADDRESS = (byte) 0x11;
	
	private ScreenBuffer buffer;
	private boolean debug = false;
	
	public DataStreamBuilder(ScreenBuffer buffer) {
		this.buffer = buffer;
	}
	
	public byte[] build(byte aid) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        stream.write(aid);
		
		if(debug) {
			System.out.println("--> " + (aid & 0xFF) + " command key");
			System.out.println("-------- hasFields: " + buffer.hasFields() + " -------------");
		}
		
		if(aid == CommandKey.CLEAR.getValue()) { // just clear screen and send aid
//			clearFlag = true;
			buffer.clear();
		}else if(!buffer.hasFields()) { // && !buffer.clearFlag) { // no fields defined, send all modified characters
				// or if clear command was sent before, send all characters
			stream.write(Tn3270Conversions.encodeAddress(buffer.cursorPosition()));
			
			if(debug) {
				System.out.println("--> " + (Tn3270Conversions.encodeAddress(buffer.cursorPosition())[0] &0xff) + " high byte");
				System.out.println("--> " + (Tn3270Conversions.encodeAddress(buffer.cursorPosition())[1] &0xff) + " low byte");
			}
			
			boolean order_sba = false;
			for(int pos=0;pos<buffer.getTotal();pos++){
				if(buffer.getAttribute(pos).isModified()) {
					if(order_sba) {
						stream.write(Tn3270Conversions.asciiToEbcdic(buffer.asciiAt(pos)));
						
						if(debug) {
							System.out.println("--> " + (int)buffer.asciiAt(pos) + " '" + buffer.asciiAt(pos) + "'");
						}
					} else {
						stream.write(ORDER_SET_BUFFER_ADDRESS);
    					stream.write(Tn3270Conversions.encodeAddress(pos));
    					stream.write(Tn3270Conversions.asciiToEbcdic(buffer.asciiAt(pos)));
    					order_sba = true;
    					
    					if(debug) {
							System.out.println("--> " + (ORDER_SET_BUFFER_ADDRESS & 0xFF) + " ORD_SBA");
							System.out.println("--> " + (Tn3270Conversions.encodeAddress(pos)[0] & 0xff) + " high byte");
							System.out.println("--> " + (Tn3270Conversions.encodeAddress(pos)[1] & 0xff) + " low byte");
							System.out.println("--> " + pos + " final position");
							System.out.println("--> " + (int)buffer.asciiAt(pos) + " '" + buffer.asciiAt(pos) + "'");
						}
					}	
				}else{
					order_sba = false;// ORDER_SBA needs to be sent again
				}
			}
		}else{	
			stream.write(Tn3270Conversions.encodeAddress(buffer.cursorPosition()));
			
			if(debug) {
				System.out.println("--> " + (Tn3270Conversions.encodeAddress(buffer.cursorPosition())[0] &0xff) + " high byte");
				System.out.println("--> " + (Tn3270Conversions.encodeAddress(buffer.cursorPosition())[1] &0xff) + " low byte");
			}
			
			Field[] fields = buffer.fields();
			
//			System.out.println("Number of fields: " + (fields != null ? fields.length : 0));

			if(fields != null){
    			for(Field field:fields){
//    				System.out.println(field);
    				if(field.attribute().isModified() && field.length()>0){
    					stream.write(ORDER_SET_BUFFER_ADDRESS);
    					stream.write(Tn3270Conversions.encodeAddress(field.position()+1)); // move to first character position of field
    					
    					if(debug) {
							System.out.println("--> " + (ORDER_SET_BUFFER_ADDRESS & 0xFF) + " ORD_SBA");
							System.out.println("--> " + (Tn3270Conversions.encodeAddress(field.position()+1)[0] &0xff) + " high byte");
							System.out.println("--> " + (Tn3270Conversions.encodeAddress(field.position()+1)[1] &0xff) + " low byte");
						}
    					
    					// write all characters of field until next field or length is reached
    					for(int i=field.position()+1;i<buffer.getTotal();i++){
    						if(buffer.getField(i) != null && buffer.getField(i) == field.position()){
    							//if(buffer.asciiAt(i) != ' '){
									
	    							stream.write(Tn3270Conversions.asciiToEbcdic(buffer.asciiAt(i)));
	    							
	    							if(debug) {
	    								System.out.println("--> " + (int)buffer.asciiAt(i) + " '" + buffer.asciiAt(i) + "'");
	    							}
//    							} else {
//    								continue;
//    							}

    							// if i remember right this logic used a 2nd ebcdic buffer to check 
    							// for modified characters, to reduce sending the trailing spaces
//								if(ebcdicBuffer[i]!=0){
//									b.put(ebcdicBuffer[i]);
//									if(debug) {
//										System.out.println("--> " + asciiBuffer[i] + " '" + (char)asciiBuffer[i] + "'");
//									}
//								}else{
//									continue;
//								}
							} else {
    							break;
    						}
    					}
    				}
    			}
			}
		}
		
		stream.write((byte)TelnetCommand.IAC);// telnet IAC
		stream.write((byte)TelnetCommand.EOR);// telnet EOR option 239
		
		if(debug) {
			System.out.println("--> " + TelnetCommand.IAC + " IAC");
			System.out.println("--> " + TelnetCommand.EOR + " EOR");
		}
		
		byte[] vals =stream.toByteArray();

//		for(byte b:vals) {
//			if(debug) {
//				System.out.println((b & 0xFF));
//			}
//		}
		
		return vals;
//        return stream.toByteArray();
		
//		ack = 0;// reset ack count
//		awaitEor()
	}
	
	
}

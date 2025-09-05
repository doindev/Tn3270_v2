package org.me.telnet;

public class SimpleOptionHandler extends TelnetOptionHandler {
    
    public SimpleOptionHandler(int optcode) {
        super(optcode, false, false, false, false);
    }
    
    public SimpleOptionHandler(int optcode, boolean initlocal, boolean initremote,
                                boolean acceptlocal, boolean acceptremote) {
        super(optcode, initlocal, initremote, acceptlocal, acceptremote);
    }
    
    @Override
    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        return null;
    }
}
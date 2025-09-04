package org.metoo.telnet;

public class SuppressGAOptionHandler extends TelnetOptionHandler {
    
    public SuppressGAOptionHandler(boolean initLocal, boolean initRemote,
                                    boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.SUPPRESS_GO_AHEAD, initLocal, initRemote, acceptLocal, acceptRemote);
    }
    
    public SuppressGAOptionHandler() {
        super(TelnetOption.SUPPRESS_GO_AHEAD, false, false, false, false);
    }
    
    @Override
    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        return null;
    }
}
package org.me.telnet;

public class EchoOptionHandler extends TelnetOptionHandler {
    
    public EchoOptionHandler(boolean initLocal, boolean initRemote,
                             boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.ECHO, initLocal, initRemote, acceptLocal, acceptRemote);
    }
    
    public EchoOptionHandler() {
        super(TelnetOption.ECHO, false, false, false, false);
    }
    
    @Override
    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        return null;
    }
}
package org.metoo.telnet;

public class TerminalTypeOptionHandler extends TelnetOptionHandler {
    private static final int TERMINAL_TYPE_IS = 0;
    private static final int TERMINAL_TYPE_SEND = 1;
    
    private String terminalType = "VT100";
    
    public TerminalTypeOptionHandler(String termtype, boolean initLocal, boolean initRemote,
                                      boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.TERMINAL_TYPE, initLocal, initRemote, acceptLocal, acceptRemote);
        terminalType = termtype;
    }
    
    public TerminalTypeOptionHandler(String termtype) {
        super(TelnetOption.TERMINAL_TYPE, false, true, false, true);
        terminalType = termtype;
    }
    
    @Override
    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        if (suboptionData != null && suboptionLength > 1) {
            if (suboptionData[0] == TelnetOption.TERMINAL_TYPE && 
                suboptionData[1] == TERMINAL_TYPE_SEND) {
                
                String term = terminalType;
                int[] response = new int[term.length() + 2];
                response[0] = TelnetOption.TERMINAL_TYPE;
                response[1] = TERMINAL_TYPE_IS;
                
                for (int i = 0; i < term.length(); i++) {
                    response[i + 2] = term.charAt(i);
                }
                
                return response;
            }
        }
        return null;
    }
    
    @Override
    public boolean startSubnegotiationRemote() {
        return true;
    }
}
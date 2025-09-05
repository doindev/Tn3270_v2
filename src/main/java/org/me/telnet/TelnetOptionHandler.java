package org.me.telnet;

public abstract class TelnetOptionHandler {
    private int optionCode = -1;
    private boolean initialLocal = false;
    private boolean initialRemote = false;
    private boolean acceptLocal = false;
    private boolean acceptRemote = false;
    private boolean doFlag = false;
    private boolean willFlag = false;
    
    public TelnetOptionHandler(int optcode, boolean initlocal, boolean initremote,
                                boolean acceptlocal, boolean acceptremote) {
        optionCode = optcode;
        initialLocal = initlocal;
        initialRemote = initremote;
        acceptLocal = acceptlocal;
        acceptRemote = acceptremote;
    }
    
    public int getOptionCode() {
        return optionCode;
    }
    
    public boolean getAcceptLocal() {
        return acceptLocal;
    }
    
    public boolean getAcceptRemote() {
        return acceptRemote;
    }
    
    public void setAcceptLocal(boolean accept) {
        acceptLocal = accept;
    }
    
    public void setAcceptRemote(boolean accept) {
        acceptRemote = accept;
    }
    
    public boolean getInitLocal() {
        return initialLocal;
    }
    
    public boolean getInitRemote() {
        return initialRemote;
    }
    
    public void setInitLocal(boolean init) {
        initialLocal = init;
    }
    
    public void setInitRemote(boolean init) {
        initialRemote = init;
    }
    
    boolean getWill() {
        return willFlag;
    }
    
    void setWill(boolean state) {
        willFlag = state;
    }
    
    boolean getDo() {
        return doFlag;
    }
    
    void setDo(boolean state) {
        doFlag = state;
    }
    
    public boolean startSubnegotiationLocal() {
        return false;
    }
    
    public boolean startSubnegotiationRemote() {
        return false;
    }
    
    public abstract int[] answerSubnegotiation(int[] suboptionData, int suboptionLength);
}
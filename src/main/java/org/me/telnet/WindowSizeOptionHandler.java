package org.me.telnet;

public class WindowSizeOptionHandler extends TelnetOptionHandler {
    private int width = 80;
    private int height = 24;
    
    public WindowSizeOptionHandler(int width, int height, boolean initLocal, boolean initRemote,
                                    boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.NEGOTIATE_ABOUT_WINDOW_SIZE, initLocal, initRemote, acceptLocal, acceptRemote);
        this.width = width;
        this.height = height;
    }
    
    public WindowSizeOptionHandler(int width, int height) {
        super(TelnetOption.NEGOTIATE_ABOUT_WINDOW_SIZE, true, false, true, false);
        this.width = width;
        this.height = height;
    }
    
    @Override
    public int[] answerSubnegotiation(int[] suboptionData, int suboptionLength) {
        return null;
    }
    
    @Override
    public boolean startSubnegotiationLocal() {
        int[] naws = new int[5];
        naws[0] = TelnetOption.NEGOTIATE_ABOUT_WINDOW_SIZE;
        naws[1] = (width >> 8) & 0xFF;
        naws[2] = width & 0xFF;
        naws[3] = (height >> 8) & 0xFF;
        naws[4] = height & 0xFF;
        
        return true;
    }
    
    public void setWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
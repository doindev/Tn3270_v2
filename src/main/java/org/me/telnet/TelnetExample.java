package org.me.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TelnetExample {
    
    public static void main(String[] args) {
        TelnetClient telnetClient = new TelnetClient("VT100");
        
        try {
            telnetClient.addOptionHandler(new TerminalTypeOptionHandler("VT100", false, false, true, false));
            telnetClient.addOptionHandler(new EchoOptionHandler(false, false, true, false));
            telnetClient.addOptionHandler(new SuppressGAOptionHandler(true, true, true, true));
            telnetClient.addOptionHandler(new WindowSizeOptionHandler(80, 24));
            
            telnetClient.registerNotifHandler(new TelnetNotificationHandler() {
                @Override
                public void receivedNegotiation(int negotiation_code, int option_code) {
                    String negotiation = "";
                    switch(negotiation_code) {
                        case TelnetNotificationHandler.RECEIVED_DO:
                            negotiation = "DO";
                            break;
                        case TelnetNotificationHandler.RECEIVED_DONT:
                            negotiation = "DONT";
                            break;
                        case TelnetNotificationHandler.RECEIVED_WILL:
                            negotiation = "WILL";
                            break;
                        case TelnetNotificationHandler.RECEIVED_WONT:
                            negotiation = "WONT";
                            break;
                        case TelnetNotificationHandler.RECEIVED_COMMAND:
                            negotiation = "COMMAND";
                            break;
                    }
                    System.out.println("Received " + negotiation + " for option: " + 
                                       TelnetOption.getOption(option_code));
                }
            });
            
            System.out.println("Connecting to telnet server...");
            telnetClient.connect("telnet.example.com", 23);
            
            System.out.println("Connected successfully!");
            System.out.println("Local address: " + telnetClient.getLocalAddress());
            System.out.println("Remote address: " + telnetClient.getRemoteAddress());
            
            InputStream input = telnetClient.getInputStream();
            OutputStream output = telnetClient.getOutputStream();
            
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            if (bytesRead > 0) {
                System.out.println("Received: " + new String(buffer, 0, bytesRead));
            }
            
            String command = "help\r\n";
            output.write(command.getBytes());
            output.flush();
            
            Thread.sleep(1000);
            
            bytesRead = input.read(buffer);
            if (bytesRead > 0) {
                System.out.println("Response: " + new String(buffer, 0, bytesRead));
            }
            
            telnetClient.disconnect();
            System.out.println("Disconnected from server.");
            
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
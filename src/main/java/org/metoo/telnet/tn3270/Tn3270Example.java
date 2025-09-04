package org.metoo.telnet.tn3270;

import java.io.IOException;
import java.util.Scanner;

public class Tn3270Example {
    
    public static void main(String[] args) {
        String hostname = "mainframe.example.com";
        int port = 23;
        
        if (args.length >= 1) {
            hostname = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        
        Tn3270 tn3270 = new Tn3270(hostname, port);
        
        try {
            System.out.println("Connecting to " + hostname + ":" + port + "...");
            tn3270.connect();
            
            System.out.println("Connected successfully!");
            System.out.println("Terminal Type: " + tn3270.getTerminalType());
            
            Thread.sleep(2000);
            
            Screen screen = tn3270.screen();
            
            System.out.println("\n=== Screen Content ===");
            System.out.println(screen.getString());
            System.out.println("======================\n");
            
            InputField[] fields = screen.getFields();
            System.out.println("Number of input fields: " + fields.length);
            
            for (int i = 0; i < fields.length; i++) {
                InputField field = fields[i];
                System.out.println("Field " + (i + 1) + ": " +
                    "Row " + field.startRow() + ", Col " + field.startColumn() +
                    " to Row " + field.endRow() + ", Col " + field.endColumn() +
                    " (Length: " + field.length() + ", Can Input: " + field.canInput() + ")");
            }
            
            Scanner scanner = new Scanner(System.in);
            boolean running = true;
            
            while (running && tn3270.isConnected()) {
                System.out.println("\nCommands: enter, pf1-pf12, clear, quit, text <string>, show");
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                try {
                    String[] parts = input.split(" ", 2);
                    String command = parts[0].toLowerCase();
                    
                    switch (command) {
                        case "enter":
                            screen.enter();
                            Thread.sleep(500);
                            System.out.println(screen.getString());
                            break;
                        case "pf1":
                            screen.pf1();
                            break;
                        case "pf2":
                            screen.pf2();
                            break;
                        case "pf3":
                            screen.pf3();
                            break;
                        case "pf4":
                            screen.pf4();
                            break;
                        case "pf5":
                            screen.pf5();
                            break;
                        case "pf6":
                            screen.pf6();
                            break;
                        case "pf7":
                            screen.pf7();
                            break;
                        case "pf8":
                            screen.pf8();
                            break;
                        case "pf9":
                            screen.pf9();
                            break;
                        case "pf10":
                            screen.pf10();
                            break;
                        case "pf11":
                            screen.pf11();
                            break;
                        case "pf12":
                            screen.pf12();
                            break;
                        case "clear":
                            screen.clear();
                            break;
                        case "text":
                            if (parts.length > 1) {
                                screen.putString(parts[1]);
                                System.out.println("Text entered: " + parts[1]);
                            }
                            break;
                        case "show":
                            System.out.println("\n=== Current Screen ===");
                            System.out.println(screen.getString());
                            System.out.println("======================");
                            break;
                        case "quit":
                        case "exit":
                            running = false;
                            break;
                        case "tab":
                            screen.tab();
                            System.out.println("Moved to next field");
                            break;
                        case "up":
                            screen.up();
                            break;
                        case "down":
                            screen.down();
                            break;
                        case "left":
                            screen.left();
                            break;
                        case "right":
                            screen.right();
                            break;
                        default:
                            System.out.println("Unknown command: " + command);
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            scanner.close();
            
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        } finally {
            System.out.println("\nDisconnecting...");
            tn3270.disconnect();
            System.out.println("Disconnected.");
        }
    }
}
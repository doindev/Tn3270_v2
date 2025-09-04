# Telnet and TN3270 Client Library

A Java implementation of Telnet and IBM 3270 terminal emulation clients, mimicking the functionality of Apache Commons Net with additional features including SSL support and comprehensive 3270 protocol handling.

## Features

### Telnet Client
- Full Telnet protocol implementation with option negotiation
- SSL/TLS support with automatic fallback to plain sockets
- Configurable terminal types
- Support for various Telnet options (Echo, Binary, Suppress Go Ahead, Window Size, etc.)
- System proxy support
- Extensible option handler architecture

### TN3270 Client
- IBM 3270 terminal emulation (IBM-3278-2-E protocol)
- Complete 3270 data stream parsing and building
- Field-based input/output management
- Support for all function keys (PF1-PF24, PA1-PA3)
- EBCDIC to ASCII conversion
- Screen buffer manipulation with row/column addressing
- Protected and unprotected field handling
- Cursor movement and field navigation

## Installation

### Maven
Add to your `pom.xml`:
```xml
<dependency>
    <groupId>org.metoo</groupId>
    <artifactId>telnet_1</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Building from Source
```bash
git clone <repository-url>
cd telnet_1
mvn clean install
```

## Usage Examples

### Basic Telnet Connection

```java
import org.metoo.telnet.TelnetClient;
import java.io.InputStream;
import java.io.OutputStream;

public class TelnetExample {
    public static void main(String[] args) {
        TelnetClient client = new TelnetClient();
        
        try {
            // Connect to telnet server
            client.connect("telnet.example.com", 23);
            
            // Get input/output streams
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();
            
            // Send command
            String command = "ls -la\r\n";
            output.write(command.getBytes());
            output.flush();
            
            // Read response
            byte[] buffer = new byte[1024];
            int bytesRead = input.read(buffer);
            System.out.println(new String(buffer, 0, bytesRead));
            
            // Disconnect
            client.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Telnet with SSL Support

```java
import org.metoo.telnet.TelnetClient;

public class SSLTelnetExample {
    public static void main(String[] args) {
        TelnetClient client = new TelnetClient();
        
        // SSL is enabled by default (tries SSL first, falls back to plain)
        // To disable SSL:
        // client.setSslEnabled(false);
        
        // Enable system proxy settings
        client.useSystemProperties(true);
        
        try {
            client.connect("secure.example.com", 992);
            
            // Use the connection...
            
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Telnet with Option Handlers

```java
import org.metoo.telnet.*;

public class TelnetOptionsExample {
    public static void main(String[] args) {
        TelnetClient client = new TelnetClient("VT100");
        
        try {
            // Add option handlers before connecting
            client.addOptionHandler(new EchoOptionHandler(false, false, true, false));
            client.addOptionHandler(new SuppressGAOptionHandler(true, true, true, true));
            client.addOptionHandler(new TerminalTypeOptionHandler("VT100", false, false, true, false));
            client.addOptionHandler(new WindowSizeOptionHandler(80, 24));
            
            // Register notification handler
            client.registerNotifHandler(new TelnetNotificationHandler() {
                @Override
                public void receivedNegotiation(int negotiation_code, int option_code) {
                    System.out.println("Negotiation: " + negotiation_code + ", Option: " + option_code);
                }
            });
            
            client.connect("telnet.example.com", 23);
            
            // Use the connection...
            
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### IBM 3270 Terminal Connection

```java
import org.metoo.telnet.tn3270.*;

public class TN3270Example {
    public static void main(String[] args) {
        Tn3270 tn3270 = new Tn3270("mainframe.example.com", 23);
        
        try {
            // Connect to mainframe
            tn3270.connect();
            
            // Get the screen object
            Screen screen = tn3270.screen();
            
            // Wait for initial screen
            Thread.sleep(2000);
            
            // Display current screen content
            System.out.println(screen.getString());
            
            // Navigate to login screen
            screen.enter();
            Thread.sleep(1000);
            
            // Enter credentials at specific positions
            screen.putString(10, 20, "USERNAME");
            screen.tab();
            screen.putString("PASSWORD");
            
            // Submit login
            screen.enter();
            Thread.sleep(1000);
            
            // Display updated screen
            System.out.println(screen.getString());
            
            // Use function keys
            screen.pf3();  // Typically "Exit" or "Back"
            screen.clear(); // Clear screen
            
            // Disconnect
            tn3270.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Working with 3270 Fields

```java
import org.metoo.telnet.tn3270.*;

public class TN3270FieldsExample {
    public static void main(String[] args) {
        Tn3270 tn3270 = new Tn3270("mainframe.example.com", 23);
        
        try {
            tn3270.connect();
            Screen screen = tn3270.screen();
            
            Thread.sleep(2000);
            
            // Get all input fields
            InputField[] fields = screen.getFields();
            System.out.println("Number of fields: " + fields.length);
            
            // Iterate through fields
            for (InputField field : fields) {
                if (field.canInput()) {
                    System.out.println("Input field at row " + field.startRow() + 
                                     ", column " + field.startColumn() + 
                                     ", length: " + field.length());
                    
                    // Get field attribute
                    FieldAttribute attr = field.attribute();
                    System.out.println("  Type: " + attr.type());
                    System.out.println("  Intensity: " + attr.intensity());
                    System.out.println("  Modified: " + attr.isModified());
                }
            }
            
            // Navigate between fields
            screen.tab();        // Next field
            screen.tab(true);    // Previous field (shift+tab)
            
            // Get field at specific position
            InputField field = screen.getFieldAt(10, 20);
            if (field != null && field.canInput()) {
                field.clearData();
                field.replaceCharacter('A');
            }
            
            // Get screen content for specific area
            String data = screen.getString(10, 20, 30);  // row, column, length
            System.out.println("Data at position: " + data);
            
            tn3270.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Interactive 3270 Session

```java
import org.metoo.telnet.tn3270.*;
import java.util.Scanner;

public class InteractiveTN3270 {
    public static void main(String[] args) {
        Tn3270 tn3270 = new Tn3270("mainframe.example.com", 23);
        Scanner scanner = new Scanner(System.in);
        
        try {
            tn3270.connect();
            Screen screen = tn3270.screen();
            
            System.out.println("Connected to mainframe. Commands:");
            System.out.println("  enter, clear, pf1-pf12, pa1-pa3");
            System.out.println("  text <string>, show, quit");
            
            boolean running = true;
            while (running && tn3270.isConnected()) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                String[] parts = input.split(" ", 2);
                String cmd = parts[0].toLowerCase();
                
                try {
                    switch (cmd) {
                        case "enter":
                            screen.enter();
                            Thread.sleep(500);
                            System.out.println(screen.getString());
                            break;
                        case "clear":
                            screen.clear();
                            break;
                        case "pf1": screen.pf1(); break;
                        case "pf2": screen.pf2(); break;
                        case "pf3": screen.pf3(); break;
                        case "pf4": screen.pf4(); break;
                        case "pf5": screen.pf5(); break;
                        case "pf6": screen.pf6(); break;
                        case "pf7": screen.pf7(); break;
                        case "pf8": screen.pf8(); break;
                        case "pf9": screen.pf9(); break;
                        case "pf10": screen.pf10(); break;
                        case "pf11": screen.pf11(); break;
                        case "pf12": screen.pf12(); break;
                        case "pa1": screen.pa1(); break;
                        case "pa2": screen.pa2(); break;
                        case "pa3": screen.pa3(); break;
                        case "text":
                            if (parts.length > 1) {
                                screen.putString(parts[1]);
                            }
                            break;
                        case "show":
                            System.out.println("\n" + screen.getString() + "\n");
                            break;
                        case "quit":
                        case "exit":
                            running = false;
                            break;
                        default:
                            System.out.println("Unknown command: " + cmd);
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            tn3270.disconnect();
            scanner.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## API Reference

### TelnetClient

#### Key Methods:
- `connect(String hostname, int port)` - Connect to telnet server
- `disconnect()` - Close the connection
- `getInputStream()` / `getOutputStream()` - Get I/O streams
- `setSslEnabled(boolean)` - Enable/disable SSL (default: true)
- `useSystemProperties(boolean)` - Enable system proxy settings
- `addOptionHandler(TelnetOptionHandler)` - Add telnet option handler
- `setTerminalType(String)` - Set terminal type

### Tn3270

#### Key Methods:
- `connect()` - Connect to 3270 host
- `disconnect()` - Close connection
- `isConnected()` - Check connection status
- `screen()` - Get Screen object for interaction

### Screen (3270)

#### Navigation:
- `enter()`, `clear()`, `esc()` - Send commands
- `pf1()` through `pf12()` - Function keys
- `pa1()`, `pa2()`, `pa3()` - Program attention keys
- `up()`, `down()`, `left()`, `right()` - Cursor movement
- `tab()`, `tab(boolean shift)` - Field navigation

#### Data Entry:
- `putString(String text)` - Enter text at cursor
- `putString(int row, int col, String text)` - Enter text at position
- `getString()` - Get entire screen content
- `getString(int row)` - Get specific row
- `getFields()` - Get all input fields

## Requirements

- Java 8 or higher
- Maven 3.x (for building)

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]

## Support

For issues and questions, please [open an issue](repository-issues-url) on GitHub.
package lab4;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MultiThreadedTCPServer {
    private static Map<String, Socket> agentSockets = new HashMap<>();  // Track agent sockets
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        int port = 8005;
        ServerSocket serverSocket = new ServerSocket(port);
        log("INFO", "Server started and listening on port " + port);

        while (true) {
            // Wait for a client connection
            Socket socket = serverSocket.accept();
            log("INFO", "New client connected");

            // Pass the agentSockets map to the ClientHandler
            new ClientHandler(socket, agentSockets).start();
        }
    }

    // Log method with levels
    private static void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        System.out.println("[" + timestamp + "][" + level + "] " + message);
    }
}

// A separate thread for each client connection
class ClientHandler extends Thread {
    private Socket socket;
    private Map<String, Socket> agentSockets;  // Reference to the shared agentSockets map

    public ClientHandler(Socket socket, Map<String, Socket> agentSockets) {
        this.socket = socket;
        this.agentSockets = agentSockets;  // Assign the shared map reference
    }

    public void run() {
        try {
            // Get input stream to read from client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Read the message from the agent
            String message = in.readLine();
            if (message != null) {
                // Handle registration
                if (message.startsWith("REGISTER|")) {
                    String agentName = message.split("\\|")[1];
                    agentSockets.put(agentName, socket);  // Track the agent's socket
                    log("INFO", agentName + " registered with the server.");
                    out.println("REGISTRATION SUCCESSFUL");

                    // Handle messages from senders
                } else if (message.startsWith("SEND|")) {
                    String[] parts = message.split("\\|");
                    String sender = parts[1];
                    String content = parts[2];

                    // Forward the message to an even-numbered agent
                    String receiver = getReceiverForSender(sender);
                    if (receiver != null && agentSockets.containsKey(receiver)) {
                        Socket receiverSocket = agentSockets.get(receiver);
                        PrintWriter receiverOut = new PrintWriter(receiverSocket.getOutputStream(), true);
                        receiverOut.println("Message from " + sender + ": " + content);
                        log("INFO", "Forwarded message from " + sender + " to " + receiver);
                    } else {
                        log("WARN", "No receiver found for " + sender);
                    }
                }
            }

            socket.close();  // Close the connection after handling
        } catch (IOException e) {
            log("ERROR", "Error handling client: " + e.getMessage());
        }
    }

    // Helper method to get the corresponding even-numbered receiver for a sender
    private String getReceiverForSender(String sender) {
        int senderNumber = Integer.parseInt(sender.replace("Smith", ""));
        int receiverNumber = senderNumber + 1;  // Assume receiver is the next agent (even-numbered)
        String receiver = "Smith" + receiverNumber;
        return agentSockets.containsKey(receiver) ? receiver : null;
    }

    // Log method with levels
    private static void log(String level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "][" + level + "] " + message);
    }
}

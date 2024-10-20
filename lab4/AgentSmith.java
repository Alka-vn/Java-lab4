package lab4;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AgentSmith extends Agent {
    private String serverIp = "testinggracealkaload-d1cb2b8cb26acbb1.elb.eu-north-1.amazonaws.com";//"127.0.0.1";  // Local testing IP
    private int serverPort = 8005;  // Port matches server's port
    private String agentName;  // Store agent's name
    private boolean alive = true;  // Agent stays alive
    private boolean isSender;  // Flag to check if the agent is a sender or receiver
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected void setup() {
        agentName = getLocalName();
        log("INFO", agentName + " is ready.");

        // Determine if the agent is a sender (odd-numbered) or receiver (even-numbered)
        int agentNumber = getAgentNumber();
        isSender = (agentNumber % 2 != 0);

        // Register the agent with the server
        registerAgent();

        if (isSender) {
            log("INFO", agentName + " is a sender.");
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    if (alive) {
                        sendMessageToServer("Message from " + agentName);
                        block(3000);  // Send a message every 3 seconds
                    }

                    // Listen for kill messages from the coordinator
                    ACLMessage msg = receive();
                    if (msg != null && msg.getContent().equals("shutdown")) {
                        takeDown();  // Call the takeDown function for a clean shutdown
                    }
                }
            });
        } else {
            log("INFO", agentName + " is a receiver.");
            addBehaviour(new CyclicBehaviour() {
                public void action() {

                    // Listen for kill messages from the coordinator
                    ACLMessage msg = receive();
                    if (msg != null && msg.getContent().equals("shutdown")) {
                        takeDown();  // Call the takeDown function for a clean shutdown
                    }
                    if (alive) {
                        receiveMessageFromServer();
                        block(3000);  // Check for new messages every 3 seconds
                    }

                }
            });
        }
    }

    // Method to extract the agent number from the agent name (e.g., Smith5 -> 5)
    private int getAgentNumber() {
        return Integer.parseInt(agentName.replace("Smith", ""));
    }

    // Register the agent with the server
    private void registerAgent() {
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send a registration message to the server
            out.println("REGISTER|" + agentName);
            log("INFO", agentName + " registered with the server.");
        } catch (ConnectException e) {
            log("ERROR", "Connection refused during registration. Server might not be running. Retrying...");
        } catch (IOException e) {
            log("ERROR", "Error during registration: " + e.getMessage());
        }
    }

    // Send a message to the server
    private void sendMessageToServer(String content) {
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send a message to the server
            out.println("SEND|" + agentName + "|" + content);
            log("INFO", agentName + " sent: " + content);
        } catch (ConnectException e) {
            log("ERROR", "Connection refused while sending message. Server might not be running. Retrying...");
        } catch (IOException e) {
            log("ERROR", "Error sending message: " + e.getMessage());
        }
    }
    private Socket receiverSocket;
    private BufferedReader in;

    // Keep the socket open for receiving messages until shutdown
    private void receiveMessageFromServer() {
        try {
            if (receiverSocket == null || receiverSocket.isClosed()) {
                receiverSocket = new Socket(serverIp, serverPort);
                in = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
                log("INFO", agentName + " connected to server for receiving messages.");
            }

            String message = in.readLine();
            if (message != null) {
                log("INFO", agentName + " received: " + message);
            } else {
                log("WARN", agentName + " received an empty message.");
            }

        } catch (IOException e) {
            log("ERROR", "Error receiving message: " + e.getMessage());
        }
    }

    // Properly close the socket only during shutdown
    protected void takeDown() {
        log("INFO", agentName + " is shutting down.");
        alive = false;  // Stop agent actions when agent shuts down

        // Notify the server that the agent is shutting down
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send UNREGISTER message to notify the server
            out.println("UNREGISTER|" + agentName);
            log("INFO", agentName + " unregistered from the server.");

            // Close the receiving socket during shutdown
            if (receiverSocket != null && !receiverSocket.isClosed()) {
                receiverSocket.close();
                log("INFO", "Receiver socket closed.");
            }

        } catch (IOException e) {
            log("ERROR", "Error during shutdown: " + e.getMessage());
        }

        doDelete();  // Proper JADE agent termination
    }


    // Log method with levels
    private static void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        System.out.println("[" + timestamp + "][" + level + "] " + message);
    }
}

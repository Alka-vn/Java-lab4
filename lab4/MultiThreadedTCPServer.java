package lab4;

import java.io.*;
import java.net.*;
import java.util.*;

public class MultiThreadedTCPServer {
    private static Map<String, String> agentSockets = new HashMap<>();  // Track connected agents by name

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    // ClientHandler to handle each agent connection
    private static class ClientHandler extends Thread {
        private Socket socket;
        private String agentName;  // Store agent's name for logging

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Read the incoming message
                String input = in.readLine();
                AgentMessage receivedMessage = AgentMessage.fromString(input);
                agentName = receivedMessage.getAgentName();  // Get the sender's name

                // Check the performative and handle the message accordingly
                if (receivedMessage.getPerformative().equals("REGISTER")) {
                    agentSockets.put(agentName, agentName);  // Store the agent's name

                    // Create a structured REPLY message
                    AgentMessage reply = new AgentMessage("REPLY", "Server", agentName, "Registered successfully");
                    out.println(reply.toString());  // Send the reply back

                    // Log the registration
                    System.out.println("Agent registered: " + agentName);

                } else if (receivedMessage.getPerformative().equals("POKE")) {
                    String targetAgent = receivedMessage.getTargetAgent();

                    // Log the poke request
                    System.out.println(agentName + " wants to poke " + targetAgent);

                    AgentMessage reply;
                    if (agentSockets.containsKey(targetAgent)) {
                        // Target agent found, reply with success
                        reply = new AgentMessage("REPLY", "Server", agentName, "Poking back from " + targetAgent);
                    } else {
                        // Target agent not found, reply with failure
                        reply = new AgentMessage("REPLY", "Server", agentName, "Failed: agent not found");
                    }

                    // Send the reply back
                    out.println(reply.toString());

                } else if (receivedMessage.getPerformative().equals("UNREGISTER")) {
                    // Unregister the agent
                    agentSockets.remove(agentName);
                    System.out.println("Agent unregistered: " + agentName);

                    // Send back confirmation
                    AgentMessage reply = new AgentMessage("REPLY", "Server", agentName, "Unregistered successfully");
                    out.println(reply.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

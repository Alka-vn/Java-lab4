package lab4;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class AgentSmith extends Agent {
    private String serverIp = "127.0.0.1";  // Use loopback IP for local testing
    private String agentName;  // Store agent's name
    private boolean alive = true;  // Agent stays alive
    private int currentRange;  // Poke range based on agent number
    private Random rand = new Random();

    protected void setup() {
        agentName = getLocalName();  // Get the agent's name
        System.out.println(agentName + " is ready.");

        // Set initial range to agent number - 1
        currentRange = getAgentNumber() - 1;

        // Register the agent with the server
        registerAgent();

        // Continuously poke other agents
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                if (alive) {
                    String targetAgent = getRandomAgent();  // Get a random agent to poke
                    boolean success = pokeAgent(targetAgent);

                    // If the poke was successful, update the range
                    if (success) {
                        int targetAgentNumber = Integer.parseInt(targetAgent.replace("Smith", ""));
                        currentRange = Math.max(currentRange, targetAgentNumber * 2);  // Double the number or keep range the same
                        System.out.println(agentName + " successfully poked " + targetAgent + ". New range: " + currentRange);
                    }
                }

                // Listen for kill messages from the coordinator
                ACLMessage msg = receive();
                if (msg != null && msg.getContent().equals("shutdown")) {
                    takeDown();
                }

                block(3000);  // Poke every 3 seconds
            }
        });
    }

    // Method to extract the agent number from the agent name (e.g., Smith5 -> 5)
    private int getAgentNumber() {
        return Integer.parseInt(agentName.replace("Smith", ""));
    }

    // Method to register the agent with the server
    private void registerAgent() {
        try (Socket socket = new Socket(serverIp, 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Create a REGISTER message
            AgentMessage message = new AgentMessage("REGISTER", agentName, null, "Registering agent");

            // Send the REGISTER message
            out.println(message.toString());

            // Read the structured server response
            String response = in.readLine();
            AgentMessage reply = AgentMessage.fromString(response);
            System.out.println("Server response: " + reply.getContent());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to poke another agent through the server
    private boolean pokeAgent(String targetAgent) {
        try (Socket socket = new Socket(serverIp, 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Create a POKE message
            AgentMessage message = new AgentMessage("POKE", agentName, targetAgent, "Attempting to poke");

            // Log the poke request
            System.out.println(message.toString());

            // Send the POKE message to the server
            out.println(message.toString());

            // Read the structured server response
            String response = in.readLine();
            AgentMessage reply = AgentMessage.fromString(response);
            System.out.println("Server response: " + reply.getContent());

            // If the response contains "Failed", the poke was not successful
            boolean success = !reply.getContent().contains("Failed");

            // Return true if successful, false if not
            return success;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to pick a random agent number within the current range
    private String getRandomAgent() {
        int randomAgentNumber = rand.nextInt(currentRange) + 1;  // Random number between 1 and currentRange
        return "Smith" + randomAgentNumber;
    }

    // Method to simulate shutting down the agent
    protected void takeDown() {
        System.out.println(agentName + " is shutting down.");
        alive = false;  // Stop poking when agent shuts down

        // Notify the server that the agent is shutting down
        try (Socket socket = new Socket(serverIp, 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Create an UNREGISTER message
            AgentMessage message = new AgentMessage("UNREGISTER", agentName, null, "Unregistering agent");

            // Send the UNREGISTER message
            out.println(message.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        doDelete();  // Proper JADE agent termination
    }
}

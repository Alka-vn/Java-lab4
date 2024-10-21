package lab4;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class AgentSmith extends Agent {
    //private String serverIp = "127.0.0.1";  // Local IP address
    private String serverIp = "lab4gracealkaelb-0e7bb1bf2b9c3d67.elb.eu-north-1.amazonaws.com";
    private int serverPort = 8080;  // Server port
    private boolean alive = true;  // Agent stays alive until killed
    private int fibNumber;  // Random Fibonacci number to request

    protected void setup() {
        // Assign a random number for the Fibonacci request
        Random rand = new Random();
        fibNumber = rand.nextInt(1000);  // Random Fibonacci number between 0 and 39
        System.out.println(getLocalName() + " is requesting Fibonacci(" + fibNumber + ").");

        // Add behavior to communicate with the server and request Fibonacci calculation
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                // Always check for shutdown messages first
                ACLMessage msg = receive();
                if (msg != null && msg.getContent().equals("shutdown")) {
                    takeDown();  // Call takeDown() for a clean shutdown
                    return;
                }

                // If the agent is alive, request a new random Fibonacci number from the server
                if (alive) {
                    int fibNumber = rand.nextInt(40);  // Generate a random number between 0 and 39
                    requestFibonacciFromServer(fibNumber);
                }

                // Block for 5 seconds between requests
                block(5000);
            }
        });
    }

    // Method to connect to the server and request Fibonacci calculation
    private void requestFibonacciFromServer(int number) {
        try (Socket socket = new Socket(serverIp, serverPort);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send request to the server
            String request = getLocalName() + " requests Fibonacci(" + number + ")\n";
            writer.write(request);
            writer.flush();
            System.out.println(getLocalName() + " sent: " + request.trim());

            // Receive the response from the server
            String response = reader.readLine();
            if (response != null) {
                System.out.println(getLocalName() + " received: " + response);
            } else {
                System.out.println(getLocalName() + " received an empty response from the server.");
            }

        } catch (IOException e) {
            System.err.println(getLocalName() + " encountered an error: " + e.getMessage());
        }
    }

    // Proper agent shutdown method
    protected void takeDown() {
        System.out.println(getLocalName() + " is shutting down.");
        alive = false;  // Stop the agent's behavior
        doDelete();  // Properly terminate the agent
    }
}
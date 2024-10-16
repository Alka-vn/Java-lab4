package lab4;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.io.*;
import java.net.Socket;

public class AgentSmith extends Agent {
    private boolean shouldShutdown = false;  // Flag to check if agent should stop

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " is ready.");

        // Register the agent with the DF under the service type "AgentSmith"
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("AgentSmith");  // Service type
        sd.setName(getLocalName());  // Agent's name
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered with DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Add a behavior to periodically try connecting to the server
        addBehaviour(new TickerBehaviour(this, 5000) { // Every 5 seconds
            protected void onTick() {
                if (shouldShutdown) {
                    System.out.println(getLocalName() + ": Shutting down.");
                    doDelete();  // Terminate the agent
                    return;
                }

                // Usual connection behavior to the server
                try {
                    Socket socket = new Socket("ec2-13-53-123-25.eu-north-1.compute.amazonaws.com", 8080);  // Replace with your EC2 public IP
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // Send the agent's name to the server
                    out.println(getLocalName());
                    socket.close();
                    System.out.println(getLocalName() + ": Connected and name sent to the server.");
                } catch (IOException e) {
                    System.out.println(getLocalName() + ": Couldn't connect to the server.");
                }
            }
        });

        // Add a behavior to listen for shutdown message
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getContent().equals("shutdown")) {
                    shouldShutdown = true;  // Set the shutdown flag to true
                } else {
                    block();  // Block until another message arrives
                }
            }
        });
    }

    // Deregister the agent from DF when it shuts down
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + " deregistered from DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("Agent " + getLocalName() + " terminating.");
    }
}

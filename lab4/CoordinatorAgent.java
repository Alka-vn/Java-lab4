package lab4;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.lang.acl.ACLMessage;

public class CoordinatorAgent extends Agent {
    private CoordinatorAgentGUI myGui;
    private int agentCounter = 0;  // Counter to track how many agents have been created

    protected void setup() {
        // Create the GUI when the agent starts
        myGui = new CoordinatorAgentGUI(this);
        System.out.println("Coordinator Agent " + getLocalName() + " is ready.");
    }

    // Method to create a specified number of Agent Smiths
    public void createAgents(int numAgents) {
        ContainerController container = getContainerController();
        try {
            for (int i = 1; i <= numAgents; i++) {
                agentCounter++;  // Increment the agent counter
                String agentName = "Smith" + agentCounter;
                AgentController agent = container.createNewAgent(agentName, "lab4.AgentSmith", null);
                agent.start();
                System.out.println(agentName + " has been created.");
            }
            // Update the GUI with the current agent count
            myGui.updateAgentCount(agentCounter);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    // Method to send shutdown messages to all agents
    public void shutdownAllAgents() {
        for (int i = 1; i <= agentCounter; i++) {
            String agentName = "Smith" + i;
            ACLMessage shutdownMessage = new ACLMessage(ACLMessage.INFORM);
            shutdownMessage.addReceiver(new jade.core.AID(agentName, jade.core.AID.ISLOCALNAME));
            shutdownMessage.setContent("shutdown");
            send(shutdownMessage);
        }

        // Reset the agent count after shutting down all agents
        resetAgentCounter();
    }

    // Method to reset the agent counter and update the GUI
    public void resetAgentCounter() {
        agentCounter = 0;  // Reset the counter to 0
        System.out.println("Agent counter has been reset.");
        myGui.updateAgentCount(agentCounter);  // Update the GUI to show 0 agents
    }

    // Cleanup method when the Coordinator Agent is terminated
    protected void takeDown() {
        if (myGui != null) {
            myGui.dispose();  // Clean up the GUI
        }
        System.out.println("Coordinator Agent " + getLocalName() + " terminating.");
    }

    // Getter for the current agent counter
    public int getAgentCounter() {
        return agentCounter;
    }
}

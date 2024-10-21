package lab4;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.lang.acl.ACLMessage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CoordinatorAgent extends Agent {
    private CoordinatorAgentGUI myGui;
    private int agentCounter = 0;  // Counter to track how many agents have been created
    private BufferedWriter logWriter;  // Log writer to store times

    protected void setup() {
        // Create the GUI when the agent starts
        myGui = new CoordinatorAgentGUI(this);
        System.out.println("Coordinator Agent " + getLocalName() + " is ready.");
        // Initialize the log writer
        try {
            logWriter = new BufferedWriter(new FileWriter("logs/agent_creation_log.csv", true));  // Append mode
// Write the CSV header if the file is empty
            logWriter.write("Timestamp,Batch,First Agent,Last Agent,Num Agents,Time Taken (ms)\n");
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Method to create a specified number of Agent Smiths
    public void createAgents(int numAgents) {
        ContainerController container = getContainerController();
        long startTime = System.currentTimeMillis();  // Start time

        try {
            int firstAgentNumber = agentCounter + 1;  // The first agent in this batch
            for (int i = 1; i <= numAgents; i++) {
                agentCounter++;  // Increment agent counter
                String agentName = "Smith" + agentCounter;
                AgentController agent = container.createNewAgent(agentName, "lab4.AgentSmith", null);
                agent.start();
                System.out.println(agentName + " has been created.");
            }
            long endTime = System.currentTimeMillis();  // End time
            long elapsedTime = endTime - startTime;  // Time taken to create agents

            // Last agent created in this batch
            int lastAgentNumber = agentCounter;

            // Get the current timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Log the batch creation in CSV format
            logWriter.write(timestamp + "," + "Batch" + "," + "Smith" + firstAgentNumber + "," + "Smith" + lastAgentNumber + "," + numAgents + "," + elapsedTime + "\n");
            logWriter.flush();  // Ensure the log is written to the file

            // Update the GUI with the current agent count
            myGui.updateAgentCount(agentCounter);

        } catch (StaleProxyException | IOException e) {
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
        shutdownAllAgents();
        System.out.println("Coordinator Agent " + getLocalName() + " terminating.");
        // Close the log writer
        try {
            if (logWriter != null) {
                logWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter for the current agent counter
    public int getAgentCounter() {
        return agentCounter;
    }
}

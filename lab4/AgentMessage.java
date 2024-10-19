package lab4;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AgentMessage {
    private String performative;  // The type of action (POKE, REGISTER, REPLY, etc.)
    private String agentName;     // Name of the agent sending the message
    private String targetAgent;   // Name of the agent receiving the message (optional)
    private String content;       // Message content or details
    private String timestamp;     // Time when the message was created

    public AgentMessage(String performative, String agentName, String targetAgent, String content) {
        this.performative = performative;
        this.agentName = agentName;
        this.targetAgent = targetAgent;
        this.content = content;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());  // Set current time
    }

    // Getters for the fields
    public String getPerformative() {
        return performative;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getTargetAgent() {
        return targetAgent;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Convert the message to a string format to send over the network
    @Override
    public String toString() {
        return performative + "|" + agentName + "|" + targetAgent + "|" + content + "|" + timestamp;
    }

    // Parse the message back from a string format
    public static AgentMessage fromString(String message) {
        String[] parts = message.split("\\|");
        return new AgentMessage(parts[0], parts[1], parts[2], parts[3]);
    }
}

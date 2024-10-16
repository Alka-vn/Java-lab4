package lab4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class CoordinatorAgentGUI extends JFrame {
    private JTextField agentNumberField;  // Text field for number of agents to create
    private JTextField intervalField;     // Text field for interval in seconds
    private JButton createButton;         // Button to create agents
    private JButton startAutomationButton; // Button to start automation
    private JButton stopAutomationButton;  // Button to stop automation
    private JButton killAllButton;        // Button to kill all agents
    private JLabel agentCountLabel;       // Label to show the current number of agents
    private CoordinatorAgent myAgent;     // Reference to the Coordinator Agent
    private Timer automationTimer;        // Timer for automated agent creation

    public CoordinatorAgentGUI(CoordinatorAgent a) {
        super(a.getLocalName());
        myAgent = a;

        // Set up the main panel with GridBagLayout for better control over positioning
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);  // Padding around components
        c.fill = GridBagConstraints.HORIZONTAL;

        // Create components
        agentNumberField = new JTextField(10);  // Number of agents to create
        intervalField = new JTextField(10);     // Interval in seconds

        createButton = new JButton("Create Agents");
        startAutomationButton = new JButton("Start Automation");
        stopAutomationButton = new JButton("Stop Automation");
        killAllButton = new JButton("Kill All Agents");

        agentCountLabel = new JLabel("Current Agents: 0", JLabel.CENTER);  // Initialize agent count label

        // Set up components in the panel with better layout

        // Row 1: Number of agents
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(new JLabel("Number of Agents: "), c);

        c.gridx = 1;
        c.gridy = 0;
        panel.add(agentNumberField, c);

        // Row 2: Interval for automation
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Interval (Seconds): "), c);

        c.gridx = 1;
        c.gridy = 1;
        panel.add(intervalField, c);

        // Row 3: Create Agents Button
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panel.add(createButton, c);

        // Row 4: Start Automation Button
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(startAutomationButton, c);

        // Row 5: Stop Automation Button
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        panel.add(stopAutomationButton, c);

        // Row 6: Kill All Agents Button
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        panel.add(killAllButton, c);

        // Row 7: Agent Count Label
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        panel.add(agentCountLabel, c);

        // Add padding and setup the main window
        getContentPane().add(panel, BorderLayout.CENTER);
        setSize(400, 400);
        setVisible(true);

        // Functionality for Create Agents button
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    int numAgents = Integer.parseInt(agentNumberField.getText());
                    myAgent.createAgents(numAgents);  // Create agents
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(CoordinatorAgentGUI.this, "Invalid number.");
                }
            }
        });

        // Functionality for Start Automation button
        startAutomationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    int numAgents = Integer.parseInt(agentNumberField.getText());
                    int intervalSeconds = Integer.parseInt(intervalField.getText());

                    startAutomation(numAgents, intervalSeconds);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(CoordinatorAgentGUI.this, "Invalid number or interval.");
                }
            }
        });

        // Functionality for Stop Automation button
        stopAutomationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                stopAutomation();  // Stop the automation
            }
        });

        // Functionality for Kill All Agents button
        killAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                myAgent.shutdownAllAgents();  // Kill all agents
            }
        });
    }

    // Method to start automated agent creation
    private void startAutomation(int numAgents, int intervalSeconds) {
        if (automationTimer != null) {
            automationTimer.cancel();  // Cancel any existing automation
        }

        automationTimer = new Timer();
        automationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> myAgent.createAgents(numAgents));
            }
        }, 0, intervalSeconds * 1000);  // Convert seconds to milliseconds
    }

    // Method to stop the automation
    private void stopAutomation() {
        if (automationTimer != null) {
            automationTimer.cancel();  // Stop the timer
            System.out.println("Automation stopped.");
        }
    }

    // Method to update the agent count label
    public void updateAgentCount(int count) {
        agentCountLabel.setText("Current Agents: " + count);  // Update the label with the new count
    }
}

# Java-lab4
==============================================================
# Agent-Based System: Quick Start Guide

## Overview
This system consists of a **Coordinator Agent** that manages the creation of **Agent Smiths**, and a **multithreaded TCP server** that handles their connections. The agents communicate, handle tasks, and periodically connect to the server to report their status.

## Components

### 1. `CoordinatorAgent.java`
- **Purpose**: Manages agent creation, tracking, and shutdown.
- **Key Methods**:
  - `createAgents(int numAgents)`: Dynamically creates the specified number of **Agent Smiths**.
  - `shutdownAllAgents()`: Sends shutdown commands to all active agents.
  - `resetAgentCounter()`: Resets the agent counter after shutdown.
- **Additional Functionality**: Responds to **Agent Smith** requests for the current number of agents.

### 2. `CoordinatorAgentGUI.java`
- **Purpose**: Provides a GUI for controlling agent operations.
- **Features**:
  - **Create Agents**: Enter the number of agents and click to create them.
  - **Automated Agent Creation**: Set an interval and automate agent creation.
  - **Kill All Agents**: Shut down all active agents at once.

### 3. `AgentSmith.java`
- **Purpose**: Each **Agent Smith** independently connects to a TCP server, registers with the **Directory Facilitator (DF)**, and listens for shutdown commands.
- **Features**:
  - **DF Registration**: Registers itself under the service type `AgentSmith`.
  - **Server Connection**: Periodically connects to the TCP server to report its status.
  - **Shutdown Handling**: Terminates upon receiving a "shutdown" message.
- **Important Setup**:
  - **Update IP Address**: You need to update the **server IP address** in the `AgentSmith.java` file so the agents can connect to the correct server.
  
    ```java
    Socket socket = new Socket("your-server-ip", 8080);  // Update with your server's IP
    ```

### 4. `MultiThreadedTCPServer.java`
- **Purpose**: A multithreaded server that listens for agent connections and handles each in a separate thread.
- **Features**:
  - **Client Management**: Accepts multiple agents connecting simultaneously.
  - **Logging**: Logs agent connections with their names.
- **EC2 Instance**: You can run this server on an **EC2 instance** to handle agent connections remotely. Ensure that port **8080** is open in your EC2 security group.
- **Package Name**: Make sure the package name matches the directory structure of your server code on the EC2 instance. If the package is `lab4`, the directory structure should reflect that:

    ```bash
    /path/to/your/code/lab4/MultiThreadedTCPServer.java
    ```

  - Compile and run using:
  
    ```bash
    java -cp . lab4.MultiThreadedTCPServer
    ```

### 5. `TCPServer.java`
- **Purpose**: A simple TCP server that handles incoming agent connections in a single thread and sends an acknowledgment message.
- **Features**:
  - **Basic Communication**: Sends a "Connected to the server" message and closes the connection after acknowledgment.
- **Ensure Correct Package Name**: As with the multithreaded server, ensure the package structure matches the code file structure on your machine or EC2 instance.

---

## How to Run the System

1. **Run the TCP Server**:
   - Launch the server on your local machine or an **EC2 instance**:
   
     ```bash
     java -cp . lab4.MultiThreadedTCPServer
     ```
   - **For EC2**: Ensure your security group allows incoming traffic on port **8080**.
   - **Correct Package Setup**: Ensure the directory structure matches your package name (`lab4` in this case). If the package declaration is `lab4`, your files should reside in a `lab4` folder.

2. **Run JADE with Coordinator and Agents**:
   - Start the JADE platform with the **Coordinator Agent** and initial **Agent Smiths**:
   
     ```bash
     java -cp jade.jar;. jade.Boot -agents "CoordinatorAgent:lab4.CoordinatorAgent;Smith1:lab4.AgentSmith;Smith2:lab4.AgentSmith" -gui
     ```

3. **Update Agent IP Address**:
   - Ensure that each **Agent Smith** connects to the correct IP (the **EC2 instance** or local server IP). Update the IP in `AgentSmith.java` as described above.

4. **Use the GUI**:
   - **Create Agents**: Enter the number of agents and click **Create Agents**.
   - **Automate**: Set an interval and click **Start Automation** for automatic agent creation.
   - **Kill All Agents**: Click **Kill All Agents** to terminate all agents.

5. **Monitor the Server**:
   - Watch the server console for messages when agents connect and report their presence.

---

## Important Notes
- **Coordination**: The **Coordinator Agent** manages agent lifecycles, including creation and shutdown.
- **Server on EC2**: If you're running the **TCP Server** on an **EC2 instance**, ensure port **8080** is open in your security group, and the **Agent Smiths** have the correct IP address.
- **Package Names**: Ensure the **package names** match the directory structure. If you use the package `lab4`, your files should reside in a `lab4` directory to avoid runtime errors.



package lab4;
import java.io.*;
import java.net.*;

public class MultiThreadedTCPServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;  // Change the port if needed
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        while (true) {
            // Wait for a client connection
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

            // Handle the connection in a new thread
            new ClientHandler(socket).start();
        }
    }
}

// A separate thread for each client connection
class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Get input stream to read from client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read the agent's name sent from the client
            String agentName = in.readLine();
            System.out.println("Agent connected: " + agentName);

            // Close the connection after handling
            socket.close();
            System.out.println("Connection closed with: " + agentName);
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }
}

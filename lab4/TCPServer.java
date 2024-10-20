package lab4;

import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;  // Change the port if needed
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

            // Handling client in a new thread
            new ServerThread(socket).start();
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Simulate some server-side task
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Connected to the server");
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }
}

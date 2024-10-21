package lab4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is listening on port 8080");


            Random rand = new Random();
            int random_number = rand.nextInt(1000);
            String server_name = "Server_" + random_number;


            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread for each client connection
                // if cannot start new thread, just tell the user

                try {
                    new ServerThread(socket, server_name).start();
                } catch (Exception e) {
                    System.out.println("Cannot start new thread for client connection");
                    socket.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

class ServerThread extends Thread {
    private Socket socket;
    private String name;

    public ServerThread(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public void run() {
        try {
            // Receiving request from the client
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientRequest = reader.readLine();

            if (clientRequest != null) {
                System.out.println("Received request: " + clientRequest);

                // Get the number from the request (between brackets)
                int number = Integer.parseInt(clientRequest.substring(clientRequest.indexOf("(") + 1, clientRequest.indexOf(")")));

                // Performing the calculation (Fibonacci in this case)
                long result = fibonacci(number); // Example calculation

                // Sending the result back to the client
                String response = name + " says " + "Fibonacci(" + number + "): " + result + "\n";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(response);
                writer.flush();
            }
            else {
                System.out.println("Received empty request (potentially NLB health check)");
            }

            // Closing the connection after sending the response
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long fibonacci(int n) {
        if (n <= 1) return n;
        else return fibonacci(n - 1) + fibonacci(n - 2);
    }
}


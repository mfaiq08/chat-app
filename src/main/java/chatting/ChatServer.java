package whatsapp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();
    private static Map<Socket, String> clientNames = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("🚀 Server is running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Client connected: " + clientSocket.getInetAddress());

                // Start a new thread for each client
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("❗ Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            clientWriters.add(out);

            // Read client's name
            String name = in.readLine();
            clientNames.put(socket, name);
            broadcast("💬 " + name + " joined the chat", out);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(name + ": " + message);
                broadcast(name + ": " + message, out);
            }

        } catch (IOException e) {
            System.out.println("❌ A client disconnected.");
        } finally {
            clientWriters.removeIf(writer -> writer.checkError());
            String name = clientNames.get(socket);
            if (name != null) {
                broadcast("🚪 " + name + " left the chat", null);
                clientNames.remove(socket);
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static void broadcast(String message, PrintWriter exclude) {
        for (PrintWriter writer : clientWriters) {
            if (writer != exclude) {
                writer.println(message);
            }
        }
    }
}

package chatting;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<Socket, String> clientNames = new HashMap<>();
    private static Set<Socket> clientSockets = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                clientSockets.add(client);
                new ClientHandler(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read username first
                userName = in.readLine();
                clientNames.put(socket, userName);
                broadcast(">> " + userName + " has joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast("[" + userName + "]: " + message);
                }
            } catch (IOException e) {
                System.out.println(userName + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) { }
                clientSockets.remove(socket);
                clientNames.remove(socket);
                broadcast(">> " + userName + " has left the chat.");
            }
        }

        private void broadcast(String message) {
            for (Socket client : clientSockets) {
                try {
                    PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                    writer.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package npk;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class chat2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP address: ");
        String serverIP = scanner.nextLine();
        System.out.print("Enter your name: ");
        String userName = scanner.nextLine();

        try {
            Socket socket = new Socket(serverIP, 12345);
            System.out.println("Connected to chat server.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send the username to server first
            out.println(userName);

            // Thread for receiving messages
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Thread for sending messages
            while (true) {
                String message = scanner.nextLine();
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

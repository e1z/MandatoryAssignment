import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {

    static Thread IMAV;
    static OutputStream output;
    //Method to send output to server
    public static void sendServer(OutputStream output, String msgSend) {
        try {
            byte[] dataSend = msgSend.getBytes();
            output.write(dataSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Method to show if there's an active user
    public static void heartBeater(String username) {
        IMAV = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(60000);
                    String heartbeat = "IMAV";
                    sendServer(output, heartbeat);
                    System.out.println(heartbeat + " " + username);
                }

            } catch (InterruptedException e) {

            }
        }); IMAV.start();
    }
    public static void main(String[] args) throws IOException {
        System.out.println("||--CLIENT CONNECTION--||\n Waiting for connection");
        Scanner sc = new Scanner(System.in);

        System.out.println("Type your desired username: ");
        String message = "JOIN" + sc.nextLine();

        System.out.println("Type the IP for the server(type 0 for localhost): ");
        String ipConnect = args.length >= 1 ? args[0] : sc.nextLine();

        System.out.println("Type the PORT for the server: ");
        int portConnect = args.length >= 2 ? Integer.parseInt(args[1]) : sc.nextInt();

        final int SERVER_PORT = portConnect;
        final String SERVER_IP = ipConnect.equals("0") ? "127.0.0.1" : ipConnect;
        InetAddress ip = InetAddress.getByName(SERVER_IP);
        Socket socket = new Socket(ip, SERVER_PORT);
        InputStream input = socket.getInputStream();
        output = socket.getOutputStream();

        String username = message.substring(4);
        System.out.println("\nUser is connecting...");
        System.out.println("Username: " + username);
        System.out.println("Server IP: " + SERVER_IP);
        System.out.println("Server PORT: " + SERVER_PORT + "\n");

        //Connection to server
        heartBeater(username);
        Thread sender = null;
        Thread receiver = null;

        if (message.contains("JOIN")) {
            try {
                String msg = "JOIN" + username + ", " + SERVER_IP + ":" + portConnect;
                byte[] dataOut = msg.getBytes();
                output.write(dataOut);
                if (!username.matches("^[a-zA-Z0-9\\-_]{1,12}$")) {
                    System.out.println("J_ER 404: Username invalid. Connection will be terminated immediately.");
                    socket.close();
                } else {
                    System.out.println("User successfully connected to server");
                }

                if (socket.isConnected()) { sender = new Thread(() ->{

                    while (true) {
                        do {
                            Scanner send = new Scanner(System.in);
                            String msgSend = send.nextLine();
                            msgSend = msgSend.trim();
                            if (msgSend.trim().length() < 250) {
                                sendServer(output, msgSend);
                                break;
                            }
                        } while (socket.isConnected());
                    }
                });
                    sender.start();

                    receiver = new Thread(() -> {
                        try {
                            while (true) {
                                byte[] dataIncoming = new byte[1024];
                                input.read(dataIncoming);
                                String msgIncoming = new String(dataIncoming);
                                msgIncoming = msgIncoming.trim();
                                System.out.println(msgIncoming);
                                if (msgIncoming.contains("J_QUIT")) {
                                    socket.close();
                                    IMAV.stop();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    receiver.start();
                } else {
                    System.out.println("Cannot resolve username, invalid username:\n Your username may only contain letters, numbers, underscore or a hyphen and be max. 12 characters long");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            OutputStream outputStream = socket.getOutputStream();
            sendServer(outputStream, message);
        }
    }
}


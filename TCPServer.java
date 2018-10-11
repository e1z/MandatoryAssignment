import java.io.*;
import java.net.*;
import java.util.*;


public class TCPServer {

    //Method to validate that the client username, and if it's invalid it will send a message to the client and terminate connection
    public static boolean userValidate(String username, Socket socket) {
        String check = "^[a-zA-Z0-9\\-_]{1,12}$";
        if (!username.matches(check)) {
            try {
                OutputStream output = socket.getOutputStream();
                System.out.println("1");
                sendClient(output, "JR_ER 404: Username invalid. The connection has been terminated immediately");
                System.out.println("J_ER 404: Username invalid: " + username + ". The connection has been terminated immediately");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return true;
        }
    }

    public static void sendClient(OutputStream output, String msgSend) {
        try {
            byte[] dataSend = msgSend.getBytes();
            output.write(dataSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        final int PORT_CONNECT = 5656;
        ArrayList<Client> clients = new ArrayList<>();
        try {
            ServerSocket server = new ServerSocket(PORT_CONNECT);

            System.out.println(" ||--SERVER IS LIVE--||\n Waiting for connection");

            while (true) {
                Socket socket = server.accept();
                String userIp = socket.getInetAddress().getHostAddress();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                System.out.println("A user succesfully connected");
                System.out.println("Connected from IP: " + userIp);
                System.out.println("Connected from PORT: " + socket.getPort());

                byte[] data = new byte[1024];
                input.read(data);
                String msg = new String(data);
                msg = msg.trim();
                System.out.println(msg.substring(4));
                if (msg.contains("JOIN")) {
                    int indexOfComma = msg.lastIndexOf(",");
                    String username = msg.substring(4, indexOfComma);
                    Client client = new Client();

                    for (Client c : clients) {
                        if (c.getUser().equalsIgnoreCase(username)) {
                            String erMsg = "J_ER 404: Username already exist.\n Connection will terminate immediately";
                            sendClient(output, erMsg);
                            socket.close();
                        } else {
                            sendClient(output, "J_OK\n");
                        }
                    }
                    userValidate(username, socket);

                    client.setIp(socket.getInetAddress().getHostAddress());
                    client.setUser(username);
                    client.setSocket(socket);
                    client.setInput(socket.getInputStream());
                    client.setOutput(socket.getOutputStream());
                    clients.add(client);
                    String allClients = "All currently connected clients: ";
                    for (Client c : clients) {
                        allClients += c.getUser() + ", ";
                    }
                    for (Client c : clients) {
                        sendClient(c.getOutput(), allClients.substring(0, allClients.length() - 2));
                    }
                    System.out.println("Send message to server?");
                    ArrayList<Thread> recipientList = new ArrayList<>();
                    Thread receiver = new Thread(() -> {
                        while (true) {
                            try {
                                InputStream inputStream = client.getInput();
                                byte[] dataIncoming = new byte[1024];
                                inputStream.read(dataIncoming);
                                String msgIncoming = new String(dataIncoming);
                                msgIncoming = msgIncoming.trim();

                                if (msgIncoming.equalsIgnoreCase("QUIT")) {
                                    sendClient(output, "J_QUIT. Terminating all connection to server immediately");
                                    client.getSocket().close();
                                    clients.remove(client);
                                    break;
                                } else if (msgIncoming.equalsIgnoreCase("IMAV")) {
                                    client.setSecondsSinceIMAV(0);
                                    System.out.println("IMAV" + " " + username);

                                } else if (msgIncoming.trim().length() > 250) {
                                    String J_ER_long = "J_ER The message is too long to be sent" + msgIncoming.trim().length();
                                    sendClient(output, J_ER_long);
                                } else {
                                    String msgClients = client.getUser() + ": " + msgIncoming;
                                    System.out.println(msgClients);
                                    for (Client c : clients) {
                                        sendClient(c.getOutput(), msgClients);
                                    }
                                }
                                if (client.getSecondsSinceIMAV() > 10) {
                                    socket.close();
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    recipientList.add(receiver);

                    for (Thread t : recipientList) {
                        t.start();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
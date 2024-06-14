package org.example.server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {

    private static byte[] incoming = new byte[256];
    private static final int PORT = 9999;
    private static DatagramSocket socket;
    private static ArrayList<Integer> users = new ArrayList<>();
    private static final HashMap<Integer, String> userNames = new HashMap<>();
    private static final InetAddress address;

    static {
        try {
            socket = new DatagramSocket(PORT);
            address = InetAddress.getLocalHost(); // IP address
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        System.out.println("Server started on port " + PORT);

        while (true) {
            DatagramPacket packet = new DatagramPacket(incoming, incoming.length); // prepare packet
            try {
                socket.receive(packet); // receive packet
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String message = new String(packet.getData(), 0, packet.getLength()); // create a string
            System.out.println("Server received: " + message);


            if (message.startsWith("init:")) {
                String userName = message.substring(6);
                users.add(packet.getPort());
                userNames.put(packet.getPort(), userName);
            }  else if (message.startsWith("/PS ")) {
                forwardPrivateMessage(message);
            }
            // forward
            else {
                int userPort = packet.getPort();  // get port from the packet
                byte[] byteMessage = message.getBytes(); // convert the string to bytes

                // forward to all other users (except the one who sent the message)
                for (int forward_port : users) {
                    if (forward_port != userPort) {
                        DatagramPacket forward = new DatagramPacket(byteMessage, byteMessage.length, address, forward_port);
                        try {
                            socket.send(forward);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }


        }
    }

    private static void forwardPrivateMessage(String message) {
        String[] parts = message.split(" ", 3);
        String recipient = parts[1];
        String privateMessage = parts[2];

        int recipientPort = getPortForName(recipient);

        byte[] messageBytes = privateMessage.getBytes();
        InetAddress recipientAddress = address;
        DatagramPacket privatePacket = new DatagramPacket(messageBytes, messageBytes.length, recipientAddress, recipientPort);
        try {
            socket.send(privatePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Extract port from userName
    private static int getPortForName(String name) {
        for (int port : users) {
            if (userNames.get(port).equals(name)) {
                return port;
            }
        }
        return -1; // If not found
    }
}
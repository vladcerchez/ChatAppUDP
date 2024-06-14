package org.example.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;

public class ChatClient extends Application {

    private static final DatagramSocket socket;
    private static final InetAddress address;
    private static final String identifier = "Ecaterina";
    private static final int SERVER_PORT = 9999; // send to server
    private static final TextArea messageArea = new TextArea();
    private static final TextField inputBox = new TextField();

    static {
        try {
            socket = new DatagramSocket(); // init to any available port
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {

        // thread for receiving messages
        ClientThread clientThread = new ClientThread(socket, messageArea);
        clientThread.start();

        // send initialization message to the server
        byte[] uuid = ("init: " + identifier).getBytes();
        DatagramPacket initialize = new DatagramPacket(uuid, uuid.length, address, SERVER_PORT);
        socket.send(initialize);

        launch(); // launch GUI

    }

    @Override
    public void start(Stage primaryStage) {

        messageArea.setMaxWidth(500);
        messageArea.setEditable(false);


        inputBox.setMaxWidth(500);
        inputBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String input = inputBox.getText();
                if (input.startsWith("/PS ")) {
                    String[] parts = input.split(" ", 3);
                    String recipient = parts[1];
                    String message = parts[2];
                    messageArea.setText(messageArea.getText() + input + "\n");
                    inputBox.setText("");
                    sendPrivateMessage(recipient, message);
                } else {
                    String temp = identifier + ": " + input; // message to send
                    messageArea.setText(messageArea.getText() + input + "\n"); // update messages on screen
                    byte[] msg = temp.getBytes(); // convert to bytes
                    inputBox.setText(""); // remove text from input box
                    DatagramPacket send = new DatagramPacket(msg, msg.length, address, SERVER_PORT); // create a packet/send
                    try {
                        socket.send(send);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        Label infoLabel = new Label("Write your message below:");

        VBox container = new VBox(25);
        container.getChildren().addAll(messageArea, infoLabel, inputBox);
        container.setPadding(new javafx.geometry.Insets(25));

        Scene scene = new Scene(container, 550, 300);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private static void sendPrivateMessage(String recipient, String message) {
        String formattedMessage = "/PS " + recipient + " " + message;
        byte[] messageBytes = formattedMessage.getBytes();
        DatagramPacket privatePacket = new DatagramPacket(messageBytes, messageBytes.length, address, SERVER_PORT);
        try {
            socket.send(privatePacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
